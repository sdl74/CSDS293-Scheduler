package taskscheduler;

import java.util.Objects;

// data class containing only getter methods for stats tracked by ServerMonitor
public class ServerStats {

    // data variables
    private final int numTasksAttempted;
    private final int numTasksComplete;
    private final int numTasksFailed;
    private final Duration totalExecutionTime;

    public ServerStats(int attempted, int complete, int failed, Duration executionTime){
        // check for null
        Objects.requireNonNull(executionTime);

        numTasksAttempted = attempted;
        numTasksComplete = complete;
        numTasksFailed = failed;
        totalExecutionTime = executionTime;
    }

    // getter method for tasksAttempted
    public int getTasksAttempted(){
        return numTasksAttempted;
    }

    // getter method for tasksComplete
    public int getTasksCompleted(){
        return numTasksComplete;
    }

    // getter method for tasksFailed
    public int getTasksFailed(){
        return numTasksFailed;
    }

    // getter method for execution time
    public Duration getExecutionTime(){
        return totalExecutionTime;
    }
}
