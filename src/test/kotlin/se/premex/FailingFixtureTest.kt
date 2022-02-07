package se.premex

import com.google.common.truth.Truth.assertThat
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

private val fixturesDir = File("src/test/fixtures")

class FailingFixtureTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "singlemodule_fail",
            "tomlparse_fail",
        ]
    )
    fun teststuff(input: String) {
        val fixtureDir = File(fixturesDir, input)

        createRunner(fixtureDir).buildAndFail()

        assertExpectedFiles(fixtureDir)
    }

    private fun createRunner(fixtureDir: File): GradleRunner {
        val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
        File("gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)
        return GradleRunner.create()
            .withProjectDir(fixtureDir)
            .withDebug(true) // Run in-process
            .withArguments("clean", "validateOwnership", "--stacktrace", "--continue") // , versionProperty)
            .withPluginClasspath()
            .forwardOutput()
    }

    private fun assertExpectedFiles(fixtureDir: File) {
        val expectedDir = File(fixtureDir, "expected")
        if (!expectedDir.exists()) {
            throw AssertionError("Missing expected/ directory")
        }

        val expectedFiles = expectedDir.walk().filter { it.isFile }.toList()
        assertThat(expectedFiles).isNotEmpty()
        for (expectedFile in expectedFiles) {
            val actualFile = File(fixtureDir, expectedFile.relativeTo(expectedDir).toString())
            if (!actualFile.exists()) {
                throw AssertionError("Expected $actualFile but does not exist")
            }

            assertThatJson(actualFile.readText())
                .`when`(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedFile.readText())
        }
    }
}
