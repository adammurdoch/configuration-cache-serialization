package test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("javaLambda", GreetingTask.class, task -> {
            task.getSource().set(() -> "greetings from a Java lambda");
        });
    }
}
