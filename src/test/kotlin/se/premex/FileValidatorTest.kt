package se.premex

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.tomlj.Toml

class FileValidatorTest {

    @Test
    fun `validate default checks successful`() {
        val validator = FileValidator()
        val toml =
            """
                version = 1
                
                [owner]
            """.trimIndent()

        assertThat(
            FileValidator().validateOwnership(validator.defaultChecks, Toml.parse(toml, TOML_VERSION))
        ).isEqualTo(
            OwnershipValidationResult(validator.defaultChecks.validationRules.map { it to true })
        )
    }

    @Test
    fun `valid version is successful`() {
        val rules = ValidationRules(listOf(Rule.ValidVersion))

        val toml =
            """
                version = 1
            """.trimIndent()

        assertThat(
            FileValidator().validateOwnership(rules, Toml.parse(toml, TOML_VERSION))
        ).isEqualTo(
            OwnershipValidationResult(rules.validationRules.map { it to true })
        )
    }

    @Test
    fun `missing version fails`() {
        val rules = ValidationRules(listOf(Rule.ValidVersion))
        val toml = ""

        assertThat(
            FileValidator().validateOwnership(rules, Toml.parse(toml, TOML_VERSION))
        ).isEqualTo(
            OwnershipValidationResult(rules.validationRules.map { it to false })
        )
    }

    @Test
    fun `invalid version fails`() {
        val rules = ValidationRules(listOf(Rule.ValidVersion))
        val toml =
            """
                version = 2
            """.trimIndent()

        assertThat(
            FileValidator().validateOwnership(rules, Toml.parse(toml, TOML_VERSION))
        ).isEqualTo(
            OwnershipValidationResult(rules.validationRules.map { it to false })
        )
    }

    @Test
    fun `dotted key exists successful`() {
        val rules = ValidationRules(listOf(Rule.DottedKeyExists(property = "owner")))

        val toml =
            """
                [owner]
            """.trimIndent()

        assertThat(
            FileValidator().validateOwnership(rules, Toml.parse(toml, TOML_VERSION))
        ).isEqualTo(
            OwnershipValidationResult(rules.validationRules.map { it to true })
        )
    }

    @Test
    fun `dotted key exists unsuccessful`() {
        val rules = ValidationRules(listOf(Rule.DottedKeyExists(property = "owner")))

        val toml = ""
        assertThat(
            FileValidator().validateOwnership(rules, Toml.parse(toml, TOML_VERSION))
        ).isEqualTo(
            OwnershipValidationResult(rules.validationRules.map { it to false })
        )
    }
}
