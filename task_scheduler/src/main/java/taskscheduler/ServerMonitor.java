package taskscheduler;

import java.time.LocalTime;

// public class to monitor the task status within a Server
public class ServerMonitor {

    // variables to keep track of task status within a server
    private int numTasksAttempted = 0;
    private int numTasksComplete = 0;
    private int numTasksFailed = 0;
    private Duration totalExecutionTime = Duration.ofMillis(0);

    // keeps track of when the most recent task was started
    private LocalTime taskStartTime;

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

    // records the statistics about a task after it's done executing (or failed)
    public void recordTask(boolean completionStatus){
        // calculate amount of time task was running for
        Duration executionTime = Duration.timeBetween(taskStartTime, LocalTime.now());

        // clear taskStartTime
        taskStartTime = null;

        // update tasksAttempted
        numTasksAttempted++;

        // update tasksComplete or tasksFailed depending on completionStatus
        if(completionStatus)
            numTasksComplete++;
        else
            numTasksFailed++;
        
        // add executionTime to total execution time
        totalExecutionTime = totalExecutionTime.add(executionTime);
    }

    // starts tracking a task
    public void taskStarted(){
        // check to make sure the previous task was completed
        if(taskStartTime != null)
            throw new ServerException("new task started without previous task finishing");

        // record start time
        taskStartTime = LocalTime.now();
    }

    // captures all of the variables and puts them into a data class
    public ServerStats getSnapshot(){
        return new ServerStats(numTasksAttempted, numTasksComplete, numTasksFailed, totalExecutionTime);
    }
}