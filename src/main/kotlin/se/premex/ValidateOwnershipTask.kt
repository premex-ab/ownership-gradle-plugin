package se.premex

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import se.premex.toml.OwnershipFile
import java.io.File

const val FAILED_RULE_EXCEPTION_MESSAGE = "Error occurred when validating rules for OWNERSHIP.toml file"
const val FAILED_PARSING_TOML_FILE_MESSAGE = "Failure parsing OWNERSHIP.toml file"
const val MISSING_TOML_FILE_MESSAGE = "Missing codeownership file from module"

@CacheableTask
open class ValidateOwnershipTask : DefaultTask() {

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    val ownershipFiles: FileCollection = project.fileTree(
        project.projectDir
    ) { files: ConfigurableFileTree ->
        val filter = files.include("**/OWNERSHIP.toml").exclude("build/**").exclude("**/.github/**")
            .exclude("**/.bitbucket/**")

        project.subprojects.forEach {
            val relativeProjectDir = File(it.projectDir.path).relativeTo(project.rootDir)
            filter.exclude("$relativeProjectDir/*")
        }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    val moduleOwnershipFile = project.file("OWNERSHIP.toml")

    @Input
    val projectRootDir = project.rootDir.path

    @OutputFile
    val resultFile: File = project.file("build/reports/ownershipValidation/validation.json")

    @Nested
    lateinit var ownershipExtension: OwnershipExtension

    @TaskAction
    @Suppress("ThrowsCount", "LongMethod")
    fun validationTask() {
        if (!resultFile.exists()) {
            resultFile.createNewFile()
        }

        val jsonParser = Json {
            encodeDefaults = true
            explicitNulls = false
        }

        if (!ownershipExtension.validateOwnership) {
            val json = jsonParser.encodeToString(
                ValidationResultData(
                    projectPath = project.path,
                    ownershipVerificationEnabled = false,
                    valid = true,
                    errors = listOf(),
                    configurations = listOf()
                )
            )
            resultFile.writeText(json)
        } else {

            val errors = mutableListOf<String>()
            val configurations = mutableListOf<Configuration>()

            if (!moduleOwnershipFile.exists()) {
                if (ownershipExtension.generateMissingOwnershipFiles) {
                    moduleOwnershipFile.createNewFile()
                    moduleOwnershipFile.writeText(
                        "version = 1\n" + "\n" + "[owner]\n" + "user = \"" +
                            ownershipExtension.defaultOwnerForMissingOwnershipFiles + "\""
                    )
                } else {
                    throw GradleException(MISSING_TOML_FILE_MESSAGE + " " + moduleOwnershipFile.path)
                }
            }

            val validationResults = ownershipFiles.sorted().map { ownershipFile ->
                val path = ownershipFile.relativeTo(File(projectRootDir)).path

                val parsedOwnershipFile = TomlParser.parseFile(ownershipFile)

                if (parsedOwnershipFile.exception != null) {
                    errors.add(parsedOwnershipFile.exception.message ?: "unknown error")
                }

                configurations.add(Configuration(path, parsedOwnershipFile.ownershipFile))
                val validator = FileValidator()
                val ownershipValidationResult: OwnershipValidationResult = validator.validateOwnership(ownershipFile)

                ValidationResult(
                    tomlParseResult = parsedOwnershipFile,
                    hasError = parsedOwnershipFile.exception != null,
                    ownershipValidationResult = ownershipValidationResult
                )
            }

            val json = jsonParser.encodeToString(
                ValidationResultData(
                    projectPath = project.path,
                    ownershipVerificationEnabled = true,
                    valid = errors.isEmpty(),
                    errors = errors,
                    configurations = configurations,
                )
            )
            resultFile.writeText(json)

            val tomlParseFails = validationResults.filter { it.hasError }
            if (tomlParseFails.isNotEmpty()) {
                tomlParseFails.forEach {
                    logger.log(LogLevel.LIFECYCLE, it.tomlParseResult.exception?.message ?: "unknown error")
                }
                throw GradleException(FAILED_PARSING_TOML_FILE_MESSAGE)
            }

            val failedRules = validationResults.mapNotNull { it.ownershipValidationResult?.results }
                .flatMap { it -> it.filter { !it.second } }

            if (failedRules.isNotEmpty()) {
                failedRules.forEach { logger.log(LogLevel.LIFECYCLE, it.first.toString()) }
                throw GradleException(FAILED_RULE_EXCEPTION_MESSAGE)
            }
        }
    }
}

@Serializable
data class ValidationResultData(
    val projectPath: String,
    val ownershipVerificationEnabled: Boolean,
    val valid: Boolean,
    val errors: List<String>,
    val configurations: List<Configuration>,
)

@Serializable
data class Configuration(
    val path: String,
    val configuration: OwnershipFile?,
)
