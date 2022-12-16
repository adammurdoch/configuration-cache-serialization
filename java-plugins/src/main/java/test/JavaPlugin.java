package test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

public class JavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("javaLambda", GreetingTask.class, task -> {
            task.setSource(() -> "Greetings from a Java lambda");
        });
        project.getTasks().register("brokenJavaLambda", task -> {
            Configuration configuration = project.getConfigurations().create("broken");
            task.doFirst(t -> {
                System.out.println("configuration = " + configuration.getName());
            });
        });
    }
}
