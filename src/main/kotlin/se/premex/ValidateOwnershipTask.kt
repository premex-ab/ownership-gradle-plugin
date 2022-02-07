package se.premex

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import java.io.File

const val FAILED_RULE_EXCEPTION_MESSAGE = "Error occurred when validating rules for OWNERSHIP.toml file"
const val FAILED_PARSING_TOML_FILE_MESSAGE = "Failure parsing OWNERSHIP.toml file"

open class ValidateOwnershipTask : DefaultTask() {

    @InputFiles
    val ownershipFiles: FileCollection =
        project
            .fileTree(
                project.projectDir
            ) { files: ConfigurableFileTree ->
                val filter = files.include("**/OWNERSHIP.toml")
                    .exclude("build/**")
                    .exclude("**/.github/**")
                    .exclude("**/.bitbucket/**")
                val projectRootDir = project.rootDir
                project.subprojects.forEach {
                    val relativeProjectDir = File(it.projectDir.path).relativeTo(projectRootDir)
                    filter.exclude("$relativeProjectDir/*")
                }
            }

    @OutputFile
    val resultFile: File = project.file("build/reports/ownershipValidation/validation.json")

    @Nested
    lateinit var ownershipExtension: OwnershipExtension

    @TaskAction
    fun validationTask() {
        if (!resultFile.exists()) {
            resultFile.createNewFile()
        }

        val jsonParser = Json { encodeDefaults = true }

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
            val validationResults = ownershipFiles.sorted().map { ownershipFile ->
                val path = ownershipFile.relativeTo(project.rootDir).path
                val tomlParseResult: TomlParseResult = Toml.parse(ownershipFile.readText())

                val configurationJson = Json.decodeFromString<JsonObject>(tomlParseResult.toJson())

                val ownershipValidationResult: OwnershipValidationResult? = if (!tomlParseResult.hasErrors()) {
                    configurations.add(Configuration(path, configurationJson))
                    val validator = FileValidator()
                    validator.validateOwnership(ownershipFile)
                } else {
                    null
                }

                ValidationResult(
                    tomlParseResult = tomlParseResult,
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

            val tomlParseFails = validationResults.filter { it.tomlParseResult.hasErrors() }
            if (tomlParseFails.isNotEmpty()) {
                tomlParseFails.forEach { logger.log(LogLevel.LIFECYCLE, it.tomlParseResult.errors().toString()) }
                throw GradleException(FAILED_PARSING_TOML_FILE_MESSAGE)
            }

            val failedRules = validationResults
                .mapNotNull { it.ownershipValidationResult?.results }
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
    val configuration: JsonObject,
)
