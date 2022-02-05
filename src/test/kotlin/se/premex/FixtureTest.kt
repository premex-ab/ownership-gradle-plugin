package se.premex

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

private val fixturesDir = File("src/test/fixtures")

class FixtureTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "singlemodule",
            "multimodule"
        ]
    )
    fun teststuff(input: String) {
        val fixtureDir = File(fixturesDir, input)

        createRunner(fixtureDir).build()

        assertExpectedFiles(fixtureDir)

        // Ensure up-to-date functionality works.
        val secondRun = GradleRunner.create()
            .withProjectDir(fixtureDir)
            .withDebug(true) // Run in-process
            .withPluginClasspath()
            .withArguments("validateOwnership", "--stacktrace") // , versionProperty)
            .forwardOutput()
            .build()

        secondRun.tasks.filter { it.path.contains(":validateOwnership") }.forEach {
            assertEquals(UP_TO_DATE, it.outcome, "Second invocation of ${it.path}")
        }
    }

    private fun createRunner(fixtureDir: File): GradleRunner {
        val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
        File("gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)
        return GradleRunner.create()
            .withProjectDir(fixtureDir)
            .withDebug(true) // Run in-process
            .withArguments("validateOwnership", "--stacktrace", "--continue") // , versionProperty)
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
            assertThat(actualFile.readText()).isEqualTo(expectedFile.readText())
        }
    }
}