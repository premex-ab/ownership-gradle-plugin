package se.premex

import kotlinx.serialization.Serializable
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import se.premex.toml.OwnershipFile
import java.io.File


open class ValidateVcsOwnershipFilesTask : DefaultTask() {

    @InputFiles
    val gitownershipFile = project.file("build/generated/ownershipValidation/root_gitownership")

    @InputFiles
    val bitbucketownershipFile = project.file("build/generated/ownershipValidation/root_bitbucketownership")

    @InputFiles
    val bitbucketownershipVcsFile = project.file(".bitbucket/CODEOWNERS")

    @InputFiles
    val githubownershipVcsFile = project.file(".github/CODEOWNERS")

    @OutputFile
    val resultFile: File = project.file("build/reports/ownershipValidation/Vcs/validation.txt")

    @Nested
    lateinit var ownershipExtension: OwnershipExtension

    @TaskAction
    fun validationTask() {
        if(ownershipExtension.generateGithubOwners) {
            if (!gitownershipFile.exists()) {
                throw GradleException("gitownershipFile does not exists")
            }
            if (!githubownershipVcsFile.exists()) {
                throw GradleException("githubownershipVcsFile does not exists")
            }
            if (gitownershipFile.readText().equals(githubownershipVcsFile)) {
                throw GradleException("github vcs file is not identical")
            }
        }
        if(ownershipExtension.generateBitbucketOwners) {
            if (!bitbucketownershipFile.exists()) {
                throw GradleException("bitbucketownershipFile does not exists")
            }
            if (!bitbucketownershipVcsFile.exists()) {
                throw GradleException("bitbucketownershipVcsFile does not exists")
            }

            if (bitbucketownershipFile.readText().equals(bitbucketownershipVcsFile)) {
                throw GradleException("bitbucket vcs file is not identical")
            }
        }
        resultFile.writeText("all good!")
    }
}
