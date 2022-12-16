package test.groovy

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class GreetingTask extends DefaultTask {
    @Internal
    Closure source

    @TaskAction
    void run() {
        println source()
    }
}
