package se.premex

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.tomlj.Toml
import org.tomlj.TomlParseResult

open class ValidateOwnershipTask : DefaultTask() {

    @InputFile
    val ownershipFile = project.file("OWNERSHIP.toml")

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
                    configuration = null
                )
            )
            resultFile.writeText(json)
        } else {
            val result: TomlParseResult = Toml.parse(ownershipFile.readText())

            val configurationJson = Json.decodeFromString<JsonObject>(result.toJson())

            val validator = FileValidator()
            validator.validateOwnership(ownershipFile)

            val json = jsonParser.encodeToString(
                ValidationResultData(
                    projectPath = project.path,
                    ownershipVerificationEnabled = true,
                    valid = !result.hasErrors(),
                    errors = result.errors().map { it.toString() },
                    configuration = configurationJson,
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
    val configuration: JsonObject?,
)
