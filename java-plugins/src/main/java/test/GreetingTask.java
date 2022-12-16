package test;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public class GreetingTask extends DefaultTask {
    private GreetingSource source;

    @Internal
    public GreetingSource getSource() {
        return source;
    }

    public void setSource(GreetingSource source) {
        this.source = source;
    }

    @TaskAction
    public void run() {
        System.out.println(source.getGreeting());
    }
}
