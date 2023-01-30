plugins {
    kotlin("jvm") version "1.7.20"
    // https://plugins.gradle.org/docs/publish-plugin
    id("com.gradle.plugin-publish") version "0.21.0"
    id("java-gradle-plugin")
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    kotlin("plugin.serialization") version "1.7.20"
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    target.compilations.all {
        compileKotlinTask.kotlinOptions.jvmTarget = "1.8"
    }
}


repositories {
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.14.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation(gradleTestKit())
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")

    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.36.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

version = androidGitVersion.name().replace("v", "")
group = "se.premex"

gradlePlugin {
    plugins {
        create("ownership") {
            id = "se.premex.ownership"
            implementationClass = "se.premex.OwnershipPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/premex-ab/ownership-gradle-plugin"
    vcsUrl = "https://github.com/premex-ab/ownership-gradle-plugin.git"
    description = "A plugin that verifies that ownership files are created in the projects"
    tags = mutableListOf("tooling", "ownership", "premex")

    (plugins) {
        "ownership" {
            displayName = "Verify ownership files are in place in subproject and have required information"
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