package taskscheduler;

// a basic implementation of the Task interface
// SimpleTask is immutable
public class SimpleTask implements Task {
    // a unique string identifier for the task
    private final String id;

    // the estimated duration of the task
    private final Duration duration;

    // completion status
    private boolean complete = false;

    // constructor
    public SimpleTask(String newId, Duration estDuration){
        // check for null values
        if(newId == null)
            throw new NullPointerException("Task cannot be created with null id");
        if(estDuration == null)
            throw new NullPointerException("Task cannot be created without an estimated Duration");

        // initialize variables with input parameters
        id = newId;
        duration = estDuration;
    }

    // returns the unique identifier for the task
    @Override
    public String getId() {
        return id;
    }

    // attempts to execute the task (TaskException is not always thrown when task fails)
    @Override
    public void execute() throws TaskException {
        // do the task (for testing purposes, this prints out the task id)
        System.out.println("executing task: " + id);

        // set the complete flag to true
        complete = true;
    }

    // returns the completion status of the task
    @Override
    public boolean isCompleted() {
        return complete;
    }

    // returns the estimated duration of the task
    @Override
    public Duration getEstimatedDuration() {
        return duration;
    }

    // returns the default priority (MEDIUM)
    @Override
    public TaskPriority getPriority(){
        return TaskPriority.MEDIUM;
    }
}