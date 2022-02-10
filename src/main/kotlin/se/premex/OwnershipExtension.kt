package se.premex

import org.gradle.api.tasks.Input

open class OwnershipExtension(
    @Input var validateOwnership: Boolean = true,
    @Input var generateGithubOwners: Boolean = false,
    @Input var generateBitbucketOwners: Boolean = false,
    @Input var generateMissingOwnershipFiles: Boolean = false,
    @Input var defaultOwnerForMissingOwnershipFiles: String = "@ReplaceMe",
)
