plugins {
    kotlin("multiplatform") version "2.3.20"
    id("se.premex.ownership")
}

kotlin {
    jvm()
}

ownership {
    validateOwnership = true
}
