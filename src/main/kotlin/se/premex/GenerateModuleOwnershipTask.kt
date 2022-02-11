package se.premex

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import se.premex.toml.OwnershipFileResult
import java.io.File

open class GenerateModuleOwnershipTask : DefaultTask() {

    @InputFiles
    val ownershipFiles: FileCollection =
        project
            .fileTree(
                project.projectDir
            ) { files: ConfigurableFileTree ->
                val filter = files.include("**/OWNERSHIP.toml")
                    .exclude("build/**")
                    .exclude("**/.github/**")
                    .exclude("**/.bitbucket/**")
                val projectRootDir = project.rootDir
                project.subprojects.forEach {
                    val relativeProjectDir = File(it.projectDir.path).relativeTo(projectRootDir)
                    filter.exclude("$relativeProjectDir/*")
                }
            }


    @OutputFile
    val gitownershipFile = project.file("build/generated/ownershipValidation/gitownership")

    @OutputFile
    val bitbucketownershipFile = project.file("build/generated/ownershipValidation/bitbucketownership")

    @Nested
    lateinit var ownershipExtension: OwnershipExtension

    @TaskAction
    fun validationTask() {
        clearFiles()

        ownershipFiles.sorted().forEach { ownershipFile ->
            var path = ownershipFile.relativeTo(project.rootProject.rootDir).path.replace("OWNERSHIP.toml", "")
            appendComment(path + "OWNERSHIP.toml")
            if (path == "") {
                path = "*"
            }

            val parsedOwnershipFile: OwnershipFileResult = TomlParser.parseFile(ownershipFile)

            appendLine(path + "\t" + parsedOwnershipFile.ownershipFile?.owner?.user!!)

            val owners: List<List<String>> = parsedOwnershipFile.ownershipFile.custom?.owners ?: listOf()
            if (owners.isNotEmpty()) {
                appendComment("Custom configurations")
            }
            owners.forEach {
                val key = it[0]
                val value = it[1]
                appendLine(key + "\t" + value)
            }
        }
        if (ownershipExtension.generateGithubOwners) {
            val target = File(project.rootProject.rootDir.path + "/.github/CODEOWNERS")
            if (target.exists()) {
                target.delete()
            }
            gitownershipFile.copyTo(target)
        }
        if (ownershipExtension.generateBitbucketOwners) {
            val target = File(project.rootProject.rootDir.path + "/.bitbucket/CODEOWNERS")
            if (target.exists()) {
                target.delete()
            }
            bitbucketownershipFile.copyTo(target)
        }
    }

    private fun clearFiles() {
        val message = ""
        if (!gitownershipFile.exists()) {
            gitownershipFile.createNewFile()
            gitownershipFile.writeText(message)
        } else {
            gitownershipFile.writeText(message)
        }
        if (!bitbucketownershipFile.exists()) {
            bitbucketownershipFile.createNewFile()
            bitbucketownershipFile.writeText(message)
        } else {
            bitbucketownershipFile.writeText(message)
        }
    }

    private fun appendLine(txt: String) {
        gitownershipFile.appendText(txt + "\n")
        bitbucketownershipFile.appendText(txt + "\n")
    }

    private fun appendComment(txt: String) {
        appendLine("# $txt")
    }
}
