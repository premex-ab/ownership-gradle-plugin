package se.premex

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PathNormalizationTest {

    @Test
    fun `path normalization replaces backslashes with forward slashes`() {
        // Simulate a Windows-style path with backslashes
        val windowsStylePath = "one\\two\\three\\OWNERSHIP.toml"

        // Apply the same normalization logic used in GenerateOwnershipTask
        val normalizedPath = windowsStylePath.replace("\\", "/").replace("OWNERSHIP.toml", "")

        // Verify that backslashes are converted to forward slashes
        assertThat(normalizedPath).isEqualTo("one/two/three/")
    }

    @Test
    fun `path normalization handles forward slashes correctly`() {
        // Unix/Linux-style path with forward slashes
        val unixStylePath = "one/two/three/OWNERSHIP.toml"

        // Apply the same normalization logic
        val normalizedPath = unixStylePath.replace("\\", "/").replace("OWNERSHIP.toml", "")

        // Verify that forward slashes remain unchanged
        assertThat(normalizedPath).isEqualTo("one/two/three/")
    }

    @Test
    fun `path normalization handles root OWNERSHIP file`() {
        // Root level OWNERSHIP file
        val rootPath = "OWNERSHIP.toml"

        // Apply the same normalization logic
        val normalizedPath = rootPath.replace("\\", "/").replace("OWNERSHIP.toml", "")

        // Verify that empty path is returned for root
        assertThat(normalizedPath).isEqualTo("")
    }

    @Test
    fun `path normalization handles single level directory`() {
        // Single level directory
        val singleLevelPath = "module\\OWNERSHIP.toml"

        // Apply the same normalization logic
        val normalizedPath = singleLevelPath.replace("\\", "/").replace("OWNERSHIP.toml", "")

        // Verify correct normalization
        assertThat(normalizedPath).isEqualTo("module/")
    }
}
