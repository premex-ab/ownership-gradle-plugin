plugins {
    kotlin("multiplatform") version "1.6.10"
    id("se.premex.ownership")
}

kotlin {
    jvm()
}

ownership {
    validateOwnership = true
}
