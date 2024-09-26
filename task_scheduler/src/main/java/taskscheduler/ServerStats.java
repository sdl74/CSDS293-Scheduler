package taskscheduler;

// view of ServerMonitor to access statistics without having full access to ServerMonitor
public interface ServerStats {

    // getter method for tasksAttempted
    public int getTasksAttempted();

    // getter method for tasksComplete
    public int getTasksCompleted();

    // getter method for tasksFailed
    public int getTasksFailed();

    // getter method for execution time
    public Duration getExecutionTime();

    // starts the collection process
    public void startTracking();
}
