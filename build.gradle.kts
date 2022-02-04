plugins {
    kotlin("jvm") version "1.6.10"
    // https://plugins.gradle.org/docs/publish-plugin
    id("com.gradle.plugin-publish") version "0.20.0"
    id("java-gradle-plugin")
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.gladed.androidgitversion") version "0.4.14"
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
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("org.tomlj:tomlj:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    testImplementation(gradleTestKit())
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

version = androidGitVersion.name().replace("v", "")

gradlePlugin {
    plugins {
        create("ownership") {
            id = "se.premex.ownership"
            group = "se.premex"
            implementationClass = "se.premex.OwnershipPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/premex-ab/ownership-gradle-plugin"
    vcsUrl = "https://github.com/premex-ab/ownership-gradle-plugin.git"
    description = "A plugin that verifies that ownership files are created in the projects"
    tags = mutableListOf("gradle", "tooling", "ownership", "premex")

    (plugins) {
        "ownership" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Verify ownership files are in place in subproject and have required information"
            description =
                """Check for existance of OWNERSHIP.toml in subprojects and verify that it contains ownership information. 
                    |
                    |Will soon also check for description and have additional tasks to generate code owner files that are understood by source control systems. 
                    |
                    |Attaches to the check 
            """.trimMargin()
        }
    }
}
