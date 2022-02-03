package se.premex

import org.tomlj.Toml
import org.tomlj.TomlParseResult
import org.tomlj.TomlVersion
import java.io.File

sealed class Rule {
    data class DottedKeyExists(val property: String) : Rule()
    object ValidVersion : Rule()
}

data class ValidationRules(val validationRules: List<Rule>)

data class ValidationResult(val results: List<Pair<Rule, Boolean>>)

val TOML_VERSION = TomlVersion.V0_5_0
val VALID_VERSIONS = arrayOf(1L)

class FileValidator {

    internal val defaultChecks = ValidationRules(
        listOf(
            Rule.DottedKeyExists(property = "owner"),
            Rule.ValidVersion,
        )
    )

    internal fun validateOwnership(tomlFile: File): ValidationResult =
        validateOwnership(defaultChecks, Toml.parse(tomlFile.reader(), TOML_VERSION))

    internal fun validateOwnership(
        validationRules: ValidationRules,
        tomlParseResult: TomlParseResult
    ): ValidationResult {
        val results = validationRules.validationRules.map {
            validateRule(tomlParseResult, it)
        }
        return ValidationResult(results)
    }

    private fun validateRule(tomlParseResult: TomlParseResult, rule: Rule): Pair<Rule, Boolean> = when (rule) {
        is Rule.DottedKeyExists -> {
            rule to tomlParseResult.contains(rule.property)
        }
        is Rule.ValidVersion -> rule to VALID_VERSIONS.contains(tomlParseResult.getLong("version"))
    }
}
