plugins {
    id("se.premex.ownership")
}

ownership {
    validateOwnership = true
}

abstract class TwoTask : DefaultTask() {
    @TaskAction
    fun greet() {
        println("fake two task")
    }
}

// Create a task using the task type
tasks.register<TwoTask>("two")