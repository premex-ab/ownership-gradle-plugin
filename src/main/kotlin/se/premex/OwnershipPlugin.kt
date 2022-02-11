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

            val genModuleTask = target.tasks.register(
                "generateModuleOwnership",
                GenerateModuleOwnershipTask::class.java
            ) { task ->
                task.ownershipExtension = ownershipExtension
            }
            genModuleTask.configure {
                it.dependsOn(validateOwnershipTask)
            }

            if (target == target.rootProject) {
                val generateOwnership = target.tasks.register(
                    "generateOwnership",
                    GenerateOwnershipTask::class.java
                ) { task ->
                    task.ownershipExtension = ownershipExtension
                }
                generateOwnership.configure { rootGenerateOwnershipTask ->
                    rootGenerateOwnershipTask.dependsOn(genModuleTask)
                    target.rootProject.subprojects.forEach { project ->
                        if (project.tasks.findByName("generateModuleOwnership") != null) {
                            project.tasks.named("generateModuleOwnership")
                                .configure { project_generateModuleOwnership ->
                                    rootGenerateOwnershipTask.dependsOn(project_generateModuleOwnership)
                                }
                        }
                    }
                }


                target.tasks.register(
                    "validateVcsOwnershipFiles",
                    ValidateVcsOwnershipFilesTask::class.java
                ) { task ->
                    task.ownershipExtension = ownershipExtension
                }.configure { rootGenerateOwnershipTask ->
                    rootGenerateOwnershipTask.dependsOn(generateOwnership)
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
