plugins {
    kotlin("jvm") version "1.6.10"
    // https://plugins.gradle.org/docs/publish-plugin
    id("com.gradle.plugin-publish") version "0.20.0"
    id("java-gradle-plugin")
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

repositories {
    mavenCentral()
}

// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
gradlePlugin {
    plugins {
        create("se.premex.ownership") {
            id = "se.premex.ownership"
            group = "se.premex"
            implementationClass = "se.premex.OwnershipPlugin"
        }
    }
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

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    testImplementation(gradleTestKit())
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    testImplementation(gradleTestKit())
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}