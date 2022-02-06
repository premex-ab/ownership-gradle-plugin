package se.premex

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import java.io.File

open class ValidateOwnershipTask : DefaultTask() {

    @InputFiles
    val ownershipFiles: FileCollection =
        project
            .fileTree(
                project.projectDir
            ) { files: ConfigurableFileTree ->
                val filter = files.include("**/OWNERSHIP.toml").exclude("build/**")
                val projectRootDir = project.rootDir
                project.subprojects.forEach {
                    val relativeProjectDir = File(it.projectDir.path).relativeTo(projectRootDir)
                    filter.exclude("$relativeProjectDir/*")
                }
            }

    @OutputFile
    val resultFile = project.file("build/reports/ownershipValidation/validation.json")

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

            ownershipFiles.forEach { ownershipFile ->
                val path = ownershipFile.relativeTo(project.rootDir).path
                val result: TomlParseResult = Toml.parse(ownershipFile.readText())

                val configurationJson = Json.decodeFromString<JsonObject>(result.toJson())

                errors.addAll(result.errors().map { it.toString() })
                configurations.add(Configuration(path, configurationJson))
                val validator = FileValidator()
                validator.validateOwnership(ownershipFile)
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
        }

        ownershipExtension
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
