plugins {
    id("java-library")
    id("se.premex.ownership")
}

ownership {
    validateOwnership = true
    generateGithubOwners = true
    generateBitbucketOwners = true
}
