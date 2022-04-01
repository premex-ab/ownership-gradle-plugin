package se.premex

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

private val fixturesDir = File("src/test/fixtures")

class FixtureTaskTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "singlemodule",
            "multimodule",
            "singlemodule_multi_ownerships",
        ]
    )
    fun checkDescriptionOfTasks(input: String) {
        val fixtureDir = File(fixturesDir, input)

        val result = createRunner(fixtureDir).build()

        assertThat(result.output).contains(
            """
            |Ownership tasks
            |---------------
            |generateOwnership - Generates the supported and configured VCS OWNERSHIP files
            |validateOwnership - Validate the content in OWNERSHIP.toml configuration files
            |""".trimMargin()
        )
    }

    private fun createRunner(fixtureDir: File): GradleRunner {
        val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
        File("gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)
        return GradleRunner.create()
            .withProjectDir(fixtureDir)
            .withDebug(true) // Run in-process
            .withArguments(
                "clean",
                "tasks"
            )
            .withPluginClasspath()
            .forwardOutput()
    }
}
