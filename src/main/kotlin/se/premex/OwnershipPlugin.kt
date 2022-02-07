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
            }

            if (target == target.rootProject) {
                target.tasks.register(
                    "generateOwnership",
                    GenerateOwnershipTask::class.java
                ) { task ->
                    task.ownershipExtension = ownershipExtension
                }.configure {
                    it.dependsOn(validateOwnershipTask)
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
