package taskscheduler;

public class PriorityTask extends SimpleTask{
    // priority
    private final TaskPriority priority;

    public PriorityTask(String newId, Duration newDuration, long realTime, TaskPriority newPriority) {
        // call super constructor
        super(newId, newDuration, realTime);

        // check if priority is not null
        if(newPriority == null)
            throw new TaskException("priority cannot be null"); // change to be NullException

        // set the priority level
        priority = newPriority;
    }

    // returns the priority of the task
    @Override
    public TaskPriority getPriority() {
        return priority;
    }
}
