package test.groovy

import org.gradle.api.Plugin
import org.gradle.api.Project

class GroovyPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.tasks.register("groovyClosure", GreetingTask) {
            it.source = { "Greetings from a Groovy closure" }
        }
    }
}
