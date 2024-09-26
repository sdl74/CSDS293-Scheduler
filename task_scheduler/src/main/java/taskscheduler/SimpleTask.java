package taskscheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

// a basic implementation of the Task interface
// SimpleTask is immutable
public class SimpleTask implements Task {
    // a unique string identifier for the task
    private final String id;

    // the estimated duration of the task
    private final Duration duration;

    // completion status
    private boolean complete = false;

    // the amount of time the task actually takes when it is executed
    private final long realDuration;

    // constructor
    public SimpleTask(String newId, Duration estDuration, long realTime){
        // check for null values
        if(newId == null)
            throw new NullPointerException("Task cannot be created with null id");
        if(estDuration == null)
            throw new NullPointerException("Task cannot be created without an estimated Duration");

        // initialize variables with input parameters
        id = newId;
        duration = estDuration;
        realDuration = realTime;
    }

    // returns the unique identifier for the task
    @Override
    public String getId() {
        return id;
    }

    // attempts to execute the task (TaskException is not always thrown when task fails)
    @Override
    public Future<Void> execute() throws TaskException {
        // force the thread to sleep for the real amount of time the task takes (to simulate the task actually executing)
        try{
            Thread.sleep(realDuration);
        }catch(InterruptedException e){
            // replace with log later
            System.out.println("task was interrupted");
        }

        // set the complete flag to true
        complete = true;

        return null;
    }

    // gives the task an opportunity to clean up resources or roll back database transactions if the task fails / gets timed out
    @Override
    public Future<Void> cleanup() throws TaskException {
        System.out.println("cleaning up task " + id);
        return null;
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
    // should notify user that default priority is medium (how should I convey this to the user in the README?)
    @Override
    public TaskPriority getPriority(){
        return TaskPriority.MEDIUM;
    }

    // returns the default, an empty set
    @Override
    public Set<String> getDependencies() {
        return new HashSet<>();
    }
}