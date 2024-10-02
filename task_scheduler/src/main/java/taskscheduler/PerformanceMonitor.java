package taskscheduler;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

// keeps track of statistics about tasks & servers
public class PerformanceMonitor {
    // logger
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    // list of most recent data for each server
    private List<ServerStats> serverStats;

    // local time when taskScheduluer.executeAllTasks() was last run
    private LocalTime startTime;

    // adds a reference to serverStats, a helper class to keep track of specific server statistics
    public void loadStatsFor(List<Server> servers){
        // check for null value
        Objects.requireNonNull(servers);

        // discard old stats
        serverStats = new ArrayList<>();

        // for each server passed, request ServerStats object and put in serverStats
        servers.stream().map(Server::getStats).forEach(stat -> {
            // try catch in case a remote server is unresponsive
            try{
                // add stats object to stats list
                serverStats.add(stat);
            }catch(ServerException e){
                // log that a server was unresponsive so its stats could not be recorded
                LOGGER.warning("a server was unresponsive so its performance monitoring stats could not be retrieved");
            }
        });
    }

    // should run function when executeAllTasks is called, it initializes the startTime variable
    public void startTracking(){
        startTime = LocalTime.now();
    }

    // calculates the average amount of time it takes a task to run (or fail)
    public Duration getAverageExecutionTime(){
        // technically checking for null, but want to notify user that loadStatsFor needs to be called to get up to date statistics
        if(serverStats == null)
            throw new NullPointerException("serverStats is null, make sure to call loadStatsFor(serverList) immediately before calling any other function to get up to date statistics");

        // get the total amount of time that each server has spent executing tasks
        long taskTime = serverStats.stream()
            .map(ServerStats::getExecutionTime)
            .reduce(Duration.ofMillis(0), (sum, newDuration) -> sum.add(newDuration))
            .toMillis();
        
        // from each server, sum number of tasks attempted
        int numTasksAttempted = serverStats.stream()
            .map(ServerStats::getTasksAttempted)
            .reduce(0, (sum, newVal) -> sum + newVal);
        
        // do check for 0 tasks attempted (do this for other methods as well)
        // divide total task time by number of tasks attempted & return as a Duration
        return Duration.ofMillis(taskTime / numTasksAttempted);
    }

    // calculates and returns the average success rate of tasks
    public float getSuccessRate(){
        // technically checking for null, but want to notify user that loadStatsFor needs to be called to get up to date statistics
        if(serverStats == null)
            throw new NullPointerException("serverStats is null, make sure to call loadStatsFor(serverList) immediately before calling any other function to get up to date statistics");

        // from all servers, sum number of successful tasks
        int numTasksCompleted = serverStats.stream()
            .map(ServerStats::getTasksCompleted)
            .reduce(0, (sum, newVal) -> sum + newVal);
        
        // from all servers, sum number of attempted tasks
        int numTasksAttempted = serverStats.stream()
            .map(ServerStats::getTasksAttempted)
            .reduce(0, (sum, newVal) -> sum + newVal);
        
        // calculate and return the success rate (successful tasks / attempted tasks)
        return (float)numTasksCompleted / numTasksAttempted;
    }

    // calculates and returns serverUtilization by dividing the amount of time each server has spent executing tasks by the amount of time spent on executeAllTasks
    public List<Double> getServerUtilization(){
        // technically checking for null, but want to notify user that loadStatsFor needs to be called to get up to date statistics
        if(serverStats == null)
            throw new NullPointerException("serverStats is null, make sure to call loadStatsFor(serverList) immediately before calling any other function to get up to date statistics");

        // holds return values
        List<Double> serverUtilization = new ArrayList<>();

        // get the amount of time that has passed since last startTracking()
        long timePassed = getTimePassed();

        // for each server, calculate the utilization & add it to serverUtilization
        serverStats.stream().forEach(s -> serverUtilization.add((double)s.getExecutionTime().toMillis() / timePassed));

        return serverUtilization;
    }

    // calculates and returns the amount of time between the last startTracking() call and now
    // unit is in milliseconds
    private long getTimePassed(){
        return startTime.until(LocalTime.now(), java.time.temporal.ChronoUnit.MILLIS);
    }
}
