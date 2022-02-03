plugins {
    id("se.premex.ownership")
}

ownership {
    validateOwnership = true
}

abstract class CheckTask : DefaultTask() {
    @TaskAction
    fun greet() {
        println("fake check task")
    }
}

// Create a task using the task type
tasks.register<CheckTask>("check")