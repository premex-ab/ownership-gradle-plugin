package se.premex

import com.google.common.truth.Truth.assertThat
import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ParseDslTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle")
    }

    @Test
    fun `verify that dsl can be properly parsed`() {

        buildFile.writeText(
            """
            apply plugin: "se.premex.ownership"

            ownership {
                validateOwnership = true
            }
        """
        )

        val project = ProjectBuilder.builder().withProjectDir(testProjectDir).build()
        (project as DefaultProject).evaluate()

        val buildOptimizationPlugin = project.plugins.getPlugin(OwnershipPlugin::class.java) as OwnershipPlugin
        val buildOptimizationPluginExtension = buildOptimizationPlugin.ownershipExtension

        assertThat(buildOptimizationPluginExtension.validateOwnership).isEqualTo(true)
    }
}
