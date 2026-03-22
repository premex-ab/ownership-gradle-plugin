plugins {
    id("java-library")
    kotlin("jvm") version "2.3.20" apply false
    id("se.premex.ownership")
}

ownership {
    validateOwnership = true
    generateGithubOwners = true
    generateBitbucketOwners = true
}
