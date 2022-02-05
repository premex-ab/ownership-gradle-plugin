plugins {
    id("se.premex.ownership")
}

ownership {
    validateOwnership = true
}

abstract class OneTask : DefaultTask() {
    @TaskAction
    fun greet() {
        println("fake one task")
    }
}

// Create a task using the task type
tasks.register<OneTask>("one")