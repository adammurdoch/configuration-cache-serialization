package test;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class GreetingTask extends DefaultTask {
    @Input
    abstract Property<GreetingSource> getSource();

    @TaskAction
    public void run() {
        System.out.println(getSource().get().getGreeting());
    }
}
