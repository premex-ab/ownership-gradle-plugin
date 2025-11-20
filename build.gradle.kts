plugins {
    kotlin("jvm") version "2.2.21"
    // https://plugins.gradle.org/docs/publish-plugin
    id("com.gradle.plugin-publish") version "1.2.1"
    id("java-gradle-plugin")
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    kotlin("plugin.serialization") version "2.1.21"
    id("com.gladed.androidgitversion") version "0.4.14"
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
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}


repositories {
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.20.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    testImplementation(gradleTestKit())
    testImplementation("com.google.truth:truth:1.4.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")

    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.38.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
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

        }
    }
}

allprojects {
    tasks.withType<ValidatePlugins>().configureEach {
        enableStricterValidation.set(true)
    }
}