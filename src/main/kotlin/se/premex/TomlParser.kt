package se.premex

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import org.gradle.api.GradleException
import se.premex.toml.OwnershipFile
import se.premex.toml.OwnershipFileResult
import java.io.File

object TomlParser {
    private val mapper: ObjectMapper = TomlMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

    internal fun parseFile(tomlFile: File): OwnershipFileResult {
        @Suppress("TooGenericExceptionCaught")
        val parsedOwnershipFile: OwnershipFileResult = try {
            OwnershipFileResult(
                exception = null,
                ownershipFile = mapper.readValue(tomlFile, OwnershipFile::class.java)
            )
        } catch (exception: Exception) {
            OwnershipFileResult(
                exception = GradleException(FAILED_PARSING_TOML_FILE_MESSAGE, exception),
                ownershipFile = null
            )
        }

        return parsedOwnershipFile
    }

    fun parseString(toml: String): OwnershipFileResult {
        @Suppress("TooGenericExceptionCaught")
        val parsedOwnershipFile: OwnershipFileResult = try {
            OwnershipFileResult(
                exception = null,
                ownershipFile = mapper.readValue(toml, OwnershipFile::class.java)
            )
        } catch (exception: Exception) {
            OwnershipFileResult(
                exception = GradleException(FAILED_PARSING_TOML_FILE_MESSAGE, exception),
                ownershipFile = null
            )
            throw GradleException()
        }

        return parsedOwnershipFile
    }
}
