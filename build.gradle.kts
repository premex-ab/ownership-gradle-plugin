plugins {
    alias(libs.plugins.kotlin.jvm)
    // https://plugins.gradle.org/docs/publish-plugin
    alias(libs.plugins.gradle.plugin.publish)
    id("java-gradle-plugin")
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidgitversion)
    id("maven-publish")
}

androidGitVersion {
    tagPattern = "^v[0-9]+.*"
}


detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
}

kotlin {
    jvmToolchain(17)
}

repositories {
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.jackson.dataformat.toml)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(gradleTestKit())
    testImplementation(libs.truth)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.junit.jupiter.params)

    testImplementation(libs.json.unit.assertj)

    testRuntimeOnly(libs.junit.jupiter.engine)

    detektPlugins(libs.detekt.formatting)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

version = androidGitVersion.name().replace("v", "")
group = "se.premex"

gradlePlugin {

    website = "https://github.com/premex-ab/ownership-gradle-plugin"
    vcsUrl = "https://github.com/premex-ab/ownership-gradle-plugin.git"
    description = "A plugin that verifies that ownership files are created in the projects"

    plugins {
        create("ownership") {
            id = "se.premex.ownership"
            implementationClass = "se.premex.OwnershipPlugin"

            displayName =
                "Verify ownership files are in place in subproject and have required information"
            description =
                """Check for existence of OWNERSHIP.toml in subprojects and verify that it contains ownership information. It will validate the syntax in the file against the toml specification
                    |
                    |Will soon also check for description and have additional tasks to generate code owner files that are understood by source control systems. 
                    |
                    |Attaches to the check task to perform validation on OWNERSHIP.toml files. 
            """.trimMargin()

            tags.set(listOf("ownership", "codeowners", "governance", "quality"))
        }
    }
}

allprojects {
    tasks.withType<ValidatePlugins>().configureEach {
        enableStricterValidation.set(true)
    }
}