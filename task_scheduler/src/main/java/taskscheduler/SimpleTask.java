package taskscheduler;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;

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

    // constructor with only id
    public SimpleTask(String newId){
        // call other constructor with newId, 0 expected duration and 0 real duration
        this(newId, Duration.ofMillis(0), 0);
    }

    // constructor
    public SimpleTask(String newId, Duration estDuration, long realTime){
        // check for null values
        Objects.requireNonNull(newId);
        Objects.requireNonNull(estDuration);

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
        // create a new thread for this task
        ExecutorService executor = Executors.newFixedThreadPool(1);
        
        return executor.submit(() -> {
            // force the thread to sleep for the real amount of time the task takes (to simulate the task actually executing)
            try{
                Thread.sleep(realDuration);
            }catch(InterruptedException e){
                // set complete to false and return early
                complete = false;
                return null;
            }

            // set the complete flag to true
            complete = true;

            return null;
        });
    }

    // gives the task an opportunity to clean up resources or roll back database transactions if the task fails / gets timed out
    @Override
    public Future<Void> cleanup() throws TaskException {
        // create a new thread for this task
        ExecutorService executor = Executors.newFixedThreadPool(1);
        
        return executor.submit(() -> {
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
        });
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
        // default priority is none (lower than low)
        return TaskPriority.NONE;
    }

    // returns the default, an empty set
    @Override
    public Set<String> getDependencies() {
        return new HashSet<>();
    }
}