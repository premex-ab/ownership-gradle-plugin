package se.premex

import org.gradle.api.tasks.Input

open class OwnershipExtension(
    @Input var validateOwnership: Boolean = true,
)
