package taskscheduler;

// an interface for all tasks, the basic unit of processing
public interface Task {
    // returns a unique id for each task
    public String getId();

    // attempts to execute the task. throws TaskException
    public void execute() throws TaskException;

    // true if the task has finished executing, false otherwise
    public boolean isCompleted();

    // returns the estimated duration of the task
    public Duration getEstimatedDuration();

    // returns the priority of the task
    public TaskPriority getPriority();
}