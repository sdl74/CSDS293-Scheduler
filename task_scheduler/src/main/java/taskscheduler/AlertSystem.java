package taskscheduler;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// communicates with TaskScheduler to alert the user when certain stats exceed thresholds
public class AlertSystem {
    // logger
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    
    // if less than this percent of tasks succeed, create an alert
    private double successRateThreshold = -1;
    // if the average amount of time it takes for a task to complete exceeds this duration, create an alert
    private Duration averageExecutionThreshold = null;
    // if the ratio of server utilization (specific utilization / total utilization) exceeds this number, create an alert
    private double maxUtilizationRatio = 1.1;

    // reference to the taskScheduler to give alerts for
    private final TaskScheduler taskScheduler;

    // thresholds will be checked every timeInterval milliseconds
    private final int timeInterval = 500;
    // these varibles set up a periodic checkStats call to check for alerts
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable periodicFunction = new Runnable(){@Override public void run(){checkStats();}};
    private final ScheduledFuture<?> runner = scheduler.scheduleAtFixedRate(periodicFunction, 0, timeInterval, TimeUnit.MILLISECONDS);

    // creates a new alert system for a given TaskScheduler
    public AlertSystem(TaskScheduler ts){
        // check for null
        Objects.requireNonNull(ts);

        // save reference to performance monitor
        taskScheduler = ts;
    }

    // sets the threshold for success rate (any value < 0 will remove the threshold)
    public void setSuccessRateThreshold(double threshold){
        successRateThreshold = threshold;
    }

    // sets the threshold for average execution time (null value will disable the threshold)
    public void setAverageExecutionThreshold(Duration threshold){
        averageExecutionThreshold = threshold;
    }

    // sets the threshold for max utilization ratio (any value > 1 will remove the threshold)
    public void setMaxUtilizationThreshold(double threshold){
        maxUtilizationRatio = threshold;
    }

    // reloads stats and checks thresholds
    private void checkStats(){
        // get updated stats from TaskScheduler (calling getStats updates the data in PerformanceMonitor)
        PerformanceMonitor pm = taskScheduler.getStats();

        // check success rate
        if(successRateBad(pm))
            alert("the average success rate of all tasks is below the threshold");

        // check average execution
        if(averageExecutionBad(pm))
            alert("the average amount of time it takes for a task to execute has exceeded the threshold");

        // check utilization ratio
        if(utilizationBad(pm))
            alert("one or more servers are exceeding the maximum ratio of utilization");
    }

    // distributes a message to any number of services (in this case, Logger & system.out.println)
    private void alert(String message){
        // send out an alert using println
        sendPrintAlert(message);

        // send out alert using logger
        logAlert(message);
    }

    // prints an alert message
    private void sendPrintAlert(String message){
        System.out.println(message);
    }

    // logs an alert message as a warning
    private void logAlert(String message){
        LOGGER.warning(message);
    }

    // will return true if the success rate falls below or equal to the set threshold
    private boolean successRateBad(PerformanceMonitor performanceMonitor){
        return  performanceMonitor.getSuccessRate() <= successRateThreshold;
    }

    // will return true if the average execution time exceeds the set threshold
    private boolean averageExecutionBad(PerformanceMonitor performanceMonitor){
        return performanceMonitor.getAverageExecutionTime().compareTo(averageExecutionThreshold) > 1;
    }

    // will return true if any server utilization ratio exceeds set threshold
    private boolean utilizationBad(PerformanceMonitor performanceMonitor){
        // get server utilization
        List<Double> serverUtilization = performanceMonitor.getServerUtilization();

        // sum total server utilization
        double totalUtilization = serverUtilization.stream().reduce(0d, (l, r) -> l + r);

        // for each server, return true if any exceed the threshold
        return serverUtilization.stream().anyMatch(su -> su / totalUtilization >= maxUtilizationRatio);
    }
}
