package taskscheduler;

import java.time.LocalTime;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

// the server class is a computational node capable of executing tasks
public class Server{
    // logger
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    // holds all tasks to be executed, separated by TaskPriority
    private Map<TaskPriority, ConcurrentLinkedQueue<Task>> taskQueues = new ConcurrentHashMap<>();

    // list of failed tasks
    private List<Task> failedTasks = new ArrayList<>();

    // keeps track of the stats for the server
    ServerMonitor serverMonitor;

    // constructor 
    public Server(){
        // create an empty list for each TaskPriority
        TaskPriority.getOrder().stream().forEach(priority -> taskQueues.put(priority, new ConcurrentLinkedQueue<>()));
    }

    // copy constructor (for defensive copying). creates a new Server that is a copy of s
    public Server(Server s){
        // check for null
        if(s == null)
            throw new NullPointerException("cannot create a copy of a null server");

        // copy all taskQueues
        this.taskQueues = new ConcurrentHashMap<>();
        for(TaskPriority p : TaskPriority.getOrder())
            this.taskQueues.put(p, new ConcurrentLinkedQueue<>(s.taskQueues.get(p)));
    }

    // adds a task to the queue
    public void addTask(Task task){
        // check for null task
        if(task == null)
            throw new NullPointerException("cannot add null task to the queue");

        // add the task to the correct queue
        taskQueues.get(task.getPriority()).add(task);
    }

    // executes all tasks in the queue and returns a list of completed tasks
    // all completed tasks will be removed from the queue
    public synchronized List<Task> executeTasks() throws ServerException {
        // log execution batch started
        LOGGER.info("server batch of tasks started");

        // list to hold all completed tasks
        List<Task> completedTasks = new ArrayList<>();

        // iterate through each priority level in order
        for(TaskPriority p : TaskPriority.getOrder()){
            // get taskList for easy reference
            AbstractQueue<Task> taskList = taskQueues.get(p);

            // execute tasks as future
            taskList.stream().forEach(task -> {
                try{
                    // tell the ServerMonitor that a task is starting
                    serverMonitor.taskStarted();

                    // log that task started
                    LOGGER.fine("task started. id: " + task.getId());

                    // execute the task with a timeout using the Future class
                    task.execute().get(Task.timeout.toMillis(), TimeUnit.MILLISECONDS);

                    // tell serverMonitor that task finished
                    serverMonitor.recordTask(true);

                    // log task completion
                    LOGGER.fine("task completed. id: " + task.getId());
                }catch(InterruptedException | ExecutionException | TimeoutException e){
                    // log failed task
                    LOGGER.warning("task timed out. id: " + task.getId());

                    // run task cleanup (also has a timeout but if this task times out, it doesn't get to clean up)
                    try{
                        task.cleanup().get(Task.timeout.toMillis(), TimeUnit.MILLISECONDS);
                    }catch(InterruptedException | ExecutionException | TimeoutException e2){
                        LOGGER.severe("task cleanup timed out. id: " + task.getId());
                    }

                    // tell serverMonitor that task failed
                    serverMonitor.recordTask(false);
                }
            });

            // add completed tasks to completedTasks
            taskList.stream().filter(Task::isCompleted).forEach(task -> completedTasks.add(task));

            // collect failed tasks
            taskList.stream().filter(t -> !t.isCompleted()).forEach(task -> failedTasks.add(task));
        }

        // clear the taskQueue by creating a new, empty one
        taskQueues = new ConcurrentHashMap<>();
        TaskPriority.getOrder().stream().forEach(priority -> taskQueues.put(priority, new ConcurrentLinkedQueue<>()));

        // return list of completed tasks
        return completedTasks;
    }

    // returns a list of all the failed tasks
    public List<Task> getFailedTasks(){
        // make defensive copy and return failedTask list
        return new ArrayList<>(failedTasks);
    }

    // returns a view of ServerMonitor
    public ServerStats getStats(){
        return serverMonitor;
    }

    // private class to monitor the task status
    private class ServerMonitor implements ServerStats {

        // variables to keep track of task status within a server
        private int numTasksAttempted = 0;
        private int numTasksComplete = 0;
        private int numTasksFailed = 0;
        private Duration totalExecutionTime = Duration.ofMillis(0);

        // keeps track of when the most recent task was started
        private LocalTime taskStartTime;

        // getter method for tasksAttempted
        @Override
        public int getTasksAttempted(){
            return numTasksAttempted;
        }

        // getter method for tasksComplete
        @Override
        public int getTasksCompleted(){
            return numTasksComplete;
        }

        // getter method for tasksFailed
        @Override
        public int getTasksFailed(){
            return numTasksFailed;
        }

        // getter method for execution time
        @Override
        public Duration getExecutionTime(){
            return totalExecutionTime;
        }

        // resets all accumulated variables
        @Override
        public void startTracking(){
            numTasksAttempted = 0;
            numTasksComplete = 0;
            numTasksFailed = 0;
            totalExecutionTime = Duration.ofMillis(0);
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
        private void taskStarted(){
            // check to make sure the previous task was completed
            if(taskStartTime != null)
                throw new ServerException("new task started without previous task finishing");

            // record start time
            taskStartTime = LocalTime.now();
        }
    }
}