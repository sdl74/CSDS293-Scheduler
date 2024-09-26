package taskscheduler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.util.logging.*;

// this class takes as input Servers and Tasks, distributes tasks to appropriate servers, then can execute all tasks
public class TaskScheduler {
    // logger
    private static final Logger LOGGER = Logger.getLogger(TaskScheduler.class.getName());

    // maps each priority to a priorityQueue containing ServerWait objects
    private Map<TaskPriority, PriorityQueue<ServerWait>> waitTimes = new EnumMap<>(TaskPriority.class);

    // holds on to tasks with dependencies until those dependencies have been completed
    private List<Dependency> dependencies = new ArrayList<>();

    // policy dictating how many retries each task gets & how the time scales
    private RetryPolicy retryPolicy;
    // priority queue keeping track of tasks to retry & when they can be retried
    private PriorityQueue<Retry> retryQueue;
    // hashmap connecting a Task id with the amount of times it has been retried
    private Map<String, Integer> taskAttempts = new HashMap<>();

    // flag true whenever there is a task scheduled to a server that hasn't been executed yet
    private boolean tasksQueued = false;

    // used to track statistics about the server
    private PerformanceMonitor performanceMonitor;

    // constructor for when you want to specify a custom retry policy
    public TaskScheduler(RetryPolicy policy){
        // check for null value
        if(policy == null)
            throw new NullPointerException("RetryPolicy cannot be null");
        
        // save retry policy
        retryPolicy = policy;

        // create blank priorityQueues for each priority in waitTimes
        TaskPriority.getOrder().forEach(p -> waitTimes.put(p, new PriorityQueue<ServerWait>()));
    }

    // constructor which uses a default retry policy (no retries and doesn't matter delay between retries)
    public TaskScheduler(){
        // call other constructor with constructed policy
        this(new RetryPolicy(0, new RetryPolicy.UniformTimeout(Duration.ofMillis(0))));
    }

    // adds a server to the list of servers (assumes server has no elements currently in taskQueue for scheduling purposes)
    public synchronized void addServer(Server server){
        // check for null
        if(server == null)
            throw new NullPointerException("cannot add null to server list");

        // log server addition
        LOGGER.info("server added to taskScheduler");

        // defensively copy server
        Server copy = new Server(server);

        // for each TaskPriority, add a new ServerWait entry to the waitTimes
        waitTimes.forEach((priority, serverList) -> serverList.add(new ServerWait(Duration.ofMillis(0), copy)));

        // add server to performance monitor
        performanceMonitor.addServerStats(copy.getStats());
    }

    // schedules a task to some available server
    public void scheduleTask(Task task){
        // check for null
        if(task == null)
            throw new NullPointerException("cannot schedule null task");

        // check to make sure there are any servers available
        if(waitTimes.isEmpty())
            throw new SchedulerFullException("no servers are available to schedule to");

        // test if task is non-dependent
        if(task.getDependencies().isEmpty())
            queueTask(task); // add task to a server
        else
            scheduleDependentTask(task); // add task to dependent task queue for later queueing
    }

    // tells all the servers to execute their tasks
    // returns a Map of Servers to their completed task list
    public synchronized Map<Server, List<Task>> executeAll() throws SchedulerException{
        // log executeAll
        LOGGER.info("scheduler executing all tasks");

        // initiate statistics collecting
        performanceMonitor.startTracking();

        // just getting a list of servers (since identical lists of ServerWait objects exist for each priority level)
        List<Server> servers = waitTimes.get(TaskPriority.HIGH).stream().map(sw -> sw.server).collect(Collectors.toList());
        
        // Map to hold return value data
        Map<Server, List<Task>> completedTasks = new HashMap<>();
        // initialize completedTasks with empty lists
        servers.stream().forEach(s -> completedTasks.put(s, new ArrayList<Task>()));

        // loop until no more tasks can run (waits for task dependencies & retries)
        while(tasksQueued){
            // holds the most recent batch of completed tasks for checking dependencies
            Map<Server, List<Task>> taskBatch = new HashMap<>();

            // execute all task queues in each server, saving the completed tasks in the taskBatch
            executeTaskBatch(servers);

            // add completed tasks to completedTasks
            servers.stream().forEach(s -> completedTasks.get(s).addAll(taskBatch.get(s)));

            // check to see if any tasks in the retry queue can be scheduled yet
            scheduleRetries();

            // collect all the failed tasks from each server and add them to the retryQueue if they can be retried
            collectFailedTasks(servers);

            // schedule tasks whose dependencies have been fulfilled
            schdeuleDependentTasks(taskBatch);

            // if no tasks are scheduled but there are still tasks in the retryQueue, wait until the next task can be executed
            if(!tasksQueued && !retryQueue.isEmpty()){
                try{
                    Thread.sleep(retryQueue.peek().getTimeUntil());
                }catch(InterruptedException e){
                    System.out.println("interrupt occurred while waiting to retry task");
                }

                // after waiting, there should be retries to schedule, so schedule them
                scheduleRetries();

                // alert if tasks didn't get scheduled by checking if no tasks are scheduled
                if(!tasksQueued)
                    System.out.println("waited to retry tasks but none were scheduledy");
            }
        }

        // log all tasks that couldn't execute due to incomplete dependencies
        dependencies.stream().map(d -> d.dependentTask).forEach(t -> LOGGER.severe("task abandoned due to incomplete dependencies. id: " + t.getId()));

        // clear dependent task list of any leftover tasks that could not execute due to failed prereqs
        dependencies = new ArrayList<>();

        // return completedTasks Map
        return completedTasks;
    }

    // getter method for the PerformanceMonitor
    public PerformanceMonitor getStats(){
        return performanceMonitor;
    }

    // tells the servers to execute one batch of tasks and returns a map of the completed tasks
    private Map<Server, List<Task>> executeTaskBatch(List<Server> servers){
        // holds completed tasks
        Map<Server, List<Task>> completed = new HashMap<>();

        // call execute on each server and collect completed tasks
        servers.stream().forEach(s -> completed.put(s, s.executeTasks()));

        // all queued tasks have been flushed, so reset flag & wait times
        tasksQueued = false;
        waitTimes.values().stream().forEach(q -> // iterate through each priority level
            q.stream().forEach(sw -> // iterate through each ServerWait
                sw.expectedWait = Duration.ofMillis(0))); // set the wait time to 0

        // return completed tasks
        return completed;
    }

    // will update the dependency information given a new batch of completed tasks & schedule tasks whose dependencies get fulfilled
    private void schdeuleDependentTasks(Map<Server, List<Task>> taskBatch){
        // first, construct a list of ids for every completed task
        List<String> completedTaskIds = taskBatch.values().stream().flatMap(list -> list.stream().map(Task::getId)).collect(Collectors.toList());

        // update the dependencies
        dependencies.stream().forEach(dep -> dep.checkForCompleteDependencies(completedTaskIds)); // update dependency status
        
        // schedule tasks with fulfilled dependencies
        dependencies.stream().filter(Dependency::canRun).forEach(t -> queueTask(t.dependentTask));
    }

    // adds a task to the dependent list
    private void scheduleDependentTask(Task task){
        // log dependent task
        LOGGER.info("task put on dependency queue, id: " + task.getId());

        // task has dependencies, create new Dependency and add to dependencies list
        Dependency d = new Dependency(task);
        // add in synchronized block so only one thread can modify dependencies at a time
        synchronized(this){dependencies.add(d);}
    }

    // schedules any tasks that can be retried at this moment in time & removes them from the retry queue
    private void scheduleRetries(){
        // check if the soonest task can be retried
        while(retryQueue.peek().canRetry()){
            // pop the task from the queue & schedule it
            queueTask(retryQueue.poll().task);
        }
    }

    // collect all the failed tasks from each server and add them to the retryQueue if they can be retried
    private void collectFailedTasks(List<Server> servers){
        // collect all the failed tasks into one list
        List<Task> failedTasks = servers.stream().map(Server::getFailedTasks).flatMap(list -> list.stream()).collect(Collectors.toList());

        // update task attempt numbers
        failedTasks.stream().map(Task::getId).forEach(id -> {
            // increment existing counts
            Integer attemptNum = taskAttempts.computeIfPresent(id, (key, value) -> value + 1); 

            // put in 1 if this is first retry (task not present in attempts list)
            if(attemptNum == null)
                taskAttempts.put(id, 1);

            // log failed task
            LOGGER.warning("task fail # " + attemptNum + ". id: " + id);
        });

        // if a task can be re-attempted, add it to the retryQueue
        failedTasks.stream().forEach(task -> {
            // get task attempt count
            int attemptNum = taskAttempts.get(task.getId());

            // if task has not exceeded attempt limit, put on the retryQueue
            if(attemptNum < retryPolicy.getMaxAttempts())
                retryQueue.add(new Retry(task, retryPolicy.getTimeoutForAttempt(taskAttempts.get(task.getId()))));
            else // log that task gets abandoned
                LOGGER.severe("task abandoned. id: " + task.getId());
        });
    }

    // this function schedules a task to a server without checking anything 
    private synchronized void queueTask(Task task){
        // log task scheduled to server
        LOGGER.info("task scheduled to server. id: " + task.getId());

        // find the server with the shortest wait of this task's priority level
        ServerWait destServer = waitTimes.get(task.getPriority()).poll();

        // add the expected duration of this task to the server wait time
        destServer.expectedWait = destServer.expectedWait.add(task.getEstimatedDuration());

        // add the task to the server
        destServer.server.addTask(task);

        // add the server wait back to the priorityQueue (which sorts it back into the queue)
        waitTimes.get(task.getPriority()).add(destServer);

        // set tasksQueued to true since a task just got queued
        tasksQueued = true;
    }

    // this is an entry class to maintain a list of servers sorted by expected wait time
    private class ServerWait implements Comparable<ServerWait> {
        // the expected wait for this entry (serves as the key)
        public Duration expectedWait;

        // the server (serves as the value)
        public Server server;

        ServerWait(Duration d, Server s){
            expectedWait = d;
            server = s;
        }

        // used to compare wait times
        @Override
        public int compareTo(ServerWait other){
            return expectedWait.compareTo(other.expectedWait);
        }
    }

    // keeps track of the dependency status of a task
    private class Dependency {
        // the task which has the dependencies
        final Task dependentTask;

        // map keeping track of the completion status of each dependency
        final Map<String, Boolean> prereqStatus = new HashMap<>();

        Dependency(Task task){
            // confirm not null
            if(task == null)
                throw new NullPointerException("dependent task cannot be null when setting up dependency");
            if(task.getDependencies() == null)
                throw new NullPointerException("prereq id Set cannot be null when setting up dependency");
            
            // initialize variables
            dependentTask = task;
            task.getDependencies().stream().forEach(depId -> prereqStatus.put(depId, false));
        }

        // this function tells whether all of the dependencies have been fulfilled yet
        public boolean canRun(){
            return prereqStatus.values().stream().reduce(true, (l, r) -> l && r);
        }

        // this method searches through a list of task ids and will set the prereqStatus for any matching ids to true
        // this doesn't change the status of previous calls to this method. so only give new task ids to this
        // input: a list of ids containing ONLY completed tasks
        public void checkForCompleteDependencies(List<String> completedTaskIds){
            // iterates through all ids and changes the prereqStatus if the id matches (using computeIfPresent)
            completedTaskIds.stream().forEach(id -> prereqStatus.computeIfPresent(id, (k, v) -> true));
        }
    }

    // keeps track of a task to retry and the timestamp it can be re-attempted after
    private class Retry implements Comparable<Retry> {

        // the batch of Tasks will not be re-attempted until this time
        public LocalTime delayUntil;

        // task to be re-attempted
        public Task task;

        // constructor
        // give a list of Tasks to retry after delay milliseconds from this current time
        public Retry(Task taskValue, Duration delay){
            // check for null values
            if(taskValue == null)
                throw new NullPointerException("task cannot be null");
            if(delay == null)
                throw new NullPointerException("delay cannot be null");
            
            // calculate the LocalTime when the task can be attempted again
            delayUntil = LocalTime.now().plus(java.time.Duration.ofMillis(delay.toMillis()));

            // save reference to task
            task = taskValue;
        }

        // compare the timeStamp to another Retry object
        @Override
        public int compareTo(Retry other){
            // use LocalTime compareTo method
            return delayUntil.compareTo(other.delayUntil);
        }

        // compares delayUntil with the current time and returns true only if the current time is after delayUntil
        public boolean canRetry(){
            return delayUntil.compareTo(LocalTime.now()) <= 0;
        }

        // returns the amount of time until the task can be retried (or 0 if the task can be executed now)
        public java.time.Duration getTimeUntil(){
            // subtract delayUntil from current time
            long timeUntil = LocalTime.now().until(delayUntil, java.time.temporal.ChronoUnit.SECONDS);

            // make sure the time is not below 0 then return as java Duration
            return java.time.Duration.ofSeconds(Math.min(0, timeUntil));
        }
    }
}