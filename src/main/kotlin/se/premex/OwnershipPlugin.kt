package se.premex

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * https://docs.gradle.org/current/userguide/custom_plugins.html
 */
class OwnershipPlugin : Plugin<Project> {
    lateinit var ownershipExtension: OwnershipExtension

    override fun apply(target: Project) {
        ownershipExtension =
            target.extensions.create("ownership", OwnershipExtension::class.java)

        target.afterEvaluate {
            val validateOwnershipTask = target.tasks.register(
                "validateOwnership",
                ValidateOwnershipTask::class.java
            ) { task ->
                task.ownershipExtension = ownershipExtension

                task.group = "Ownership"
                task.description = "Validate the content in OWNERSHIP.toml configuration files"
            }

            if (target == target.rootProject) {
                val generateOwnershipTask = target.tasks.register(
                    "generateOwnership",
                    GenerateOwnershipTask::class.java
                ) { task ->
                    task.ownershipExtension = ownershipExtension
                    task.group = "Ownership"
                    task.description = "Generates the supported and configured VCS OWNERSHIP files"
                }.configure {
                    it.dependsOn(validateOwnershipTask)
                }

                target.tasks.register("ownership") { task ->
                    task.group = "Ownership"
                    task.description = "Validates OWNERSHIP.toml files and generates VCS OWNERSHIP files"
                    task.dependsOn(generateOwnershipTask)
                }
            } else {
                target.tasks.register("ownership") { task ->
                    task.group = "Ownership"
                    task.description = "Validates OWNERSHIP.toml files"
                    task.dependsOn(validateOwnershipTask)
                }
            }

            if (target.tasks.findByName("check") != null) {
                target.tasks.named("check").configure {
                    it.dependsOn(validateOwnershipTask)
                }
            }
        }
    }
}
