package taskscheduler;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Future;

// an interface for all tasks, the basic unit of processing
public interface Task extends Serializable{
    // max amount of time a task can run before being abandoned
    public static final Duration timeout = Duration.ofMillis(5000);

    // returns a unique id for each task
    public String getId();

    // attempts to execute the task. throws TaskException
    public Future<Void> execute() throws TaskException;

    // allows tasks to be cleaned up if it is cancelled
    public Future<Void> cleanup() throws TaskException;

    // true if the task has finished executing, false otherwise
    public boolean isCompleted();

    // returns the estimated duration of the task
    public Duration getEstimatedDuration();

    // returns the priority of the task
    public TaskPriority getPriority();

    // returns a list of all dependencies for the task
    public Set<String> getDependencies();
}