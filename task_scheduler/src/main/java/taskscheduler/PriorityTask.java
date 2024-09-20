package taskscheduler;

public class PriorityTask extends SimpleTask{
    // priority
    private final TaskPriority priority;

    public PriorityTask(String newId, Duration newDuration, TaskPriority newPriority) {
        // call super constructor
        super(newId, newDuration);

        // check if priority is not null
        if(newPriority == null)
            throw new TaskException("priority cannot be null");

        // set the priority level
        priority = newPriority;
    }

    // returns the priority of the task
    @Override
    public TaskPriority getPriority() {
        return priority;
    }
}
