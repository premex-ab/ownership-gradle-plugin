package se.premex

import se.premex.toml.OwnershipFileResult
import java.io.File

sealed class Rule {
    object OwnerExists : Rule()
    object ValidVersion : Rule()
}

data class ValidationRules(val validationRules: List<Rule>)
data class OwnershipValidationResult(val results: List<Pair<Rule, Boolean>>)
data class ValidationResult(
    val tomlParseResult: OwnershipFileResult,
    val hasError: Boolean,
    val ownershipValidationResult: OwnershipValidationResult?
)

val VALID_VERSIONS = arrayOf(1L)

class FileValidator {

    internal val defaultChecks = ValidationRules(
        listOf(
            Rule.OwnerExists,
            Rule.ValidVersion,
        )
    )

    internal fun validateOwnership(tomlFile: File): OwnershipValidationResult =
        validateOwnership(defaultChecks, TomlParser.parseFile(tomlFile))

    internal fun validateOwnership(
        validationRules: ValidationRules,
        tomlParseResult: OwnershipFileResult
    ): OwnershipValidationResult {
        val results = validationRules.validationRules.map {
            validateRule(tomlParseResult, it)
        }
        return OwnershipValidationResult(results)
    }

    private fun validateRule(tomlParseResult: OwnershipFileResult, rule: Rule): Pair<Rule, Boolean> = when (rule) {
        is Rule.OwnerExists -> {
            rule to (!tomlParseResult.ownershipFile?.owner?.user.isNullOrEmpty())
        }
        is Rule.ValidVersion -> rule to VALID_VERSIONS.contains(tomlParseResult.ownershipFile?.version ?: 0)
    }
}
