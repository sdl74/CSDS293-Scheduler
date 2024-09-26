package taskscheduler;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// keeps track of statistics about tasks & servers
public class PerformanceMonitor {
    // list of views for each server
    private List<ServerStats> servers = new ArrayList<>();

    // local time when taskScheduluer.executeAllTasks() was last run
    private LocalTime startTime;

    // adds a reference to serverStats, a helper class to keep track of specific server statistics
    public void addServerStats(ServerStats server){
        // check for null value
        if(server == null)
            throw new NullPointerException("server stats cannot be null");
        
        // add ServerStats to list (no defensive copying because it is a view)
        servers.add(server);
    }

    // should run function when executeAllTasks is called, it initializes the startTime variable
    public void startTracking(){
        startTime = LocalTime.now();

        // for every server, reset collection variables
        servers.stream().forEach(ServerStats::startTracking);
    }

    // calculates the average amount of time it takes a task to run (or fail)
    public Duration getAverageExecutionTime(){
        // get the total amount of time that each server has spent executing tasks
        long taskTime = servers.stream()
            .map(ServerStats::getExecutionTime)
            .reduce(Duration.ofMillis(0), (sum, newDuration) -> sum.add(newDuration))
            .toMillis();
        
        // from each server, sum number of tasks attempted
        int numTasksAttempted = servers.stream()
            .map(ServerStats::getTasksAttempted)
            .reduce(0, (sum, newVal) -> sum + newVal);
        
        // divide total task time by number of tasks attempted & return as a Duration
        return Duration.ofMillis(taskTime / numTasksAttempted);
    }

    // calculates and returns the average success rate of tasks
    public float getSuccessRate(){
        // from all servers, sum number of successful tasks
        int numTasksCompleted = servers.stream()
            .map(ServerStats::getTasksCompleted)
            .reduce(0, (sum, newVal) -> sum + newVal);
        
        // from all servers, sum number of attempted tasks
        int numTasksAttempted = servers.stream()
            .map(ServerStats::getTasksAttempted)
            .reduce(0, (sum, newVal) -> sum + newVal);
        
        // calculate and return the success rate (successful tasks / attempted tasks)
        return (float)numTasksCompleted / numTasksAttempted;
    }

    // calculates and returns serverUtilization by dividing the amount of time each server has spent executing tasks by the amount of time spent on executeAllTasks
    // takes as input a list of servers, and server stats will be returned for each server
    public Map<Server, Float> getServerUtilization(List<Server> serverList){
        // holds return values
        Map<Server, Float> serverUtilization = new HashMap<>();

        // get the amount of time that has passed since last startTracking()
        long timePassed = getTimePassed();

        // for each server, calculate the utilization & add it to serverUtilization
        serverList.stream().forEach(s -> serverUtilization.put(s, (Float)((float)s.getStats().getExecutionTime().toMillis() / timePassed)));

        return serverUtilization;
    }

    // calculates and returns the amount of time between the last startTracking() call and now
    // unit is in milliseconds
    private long getTimePassed(){
        return startTime.until(LocalTime.now(), java.time.temporal.ChronoUnit.MILLIS);
    }
}
