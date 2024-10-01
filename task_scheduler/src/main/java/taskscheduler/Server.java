package taskscheduler;

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

        // create ServerMonitor
        serverMonitor = new ServerMonitor();
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
        
        // create serverMonitor
        serverMonitor = new ServerMonitor();
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
        return serverMonitor.getSnapshot();
    }

    // returns whether the server is reachable
    public boolean isOnline(){
        // the base Server class is local, so always reachable
        return true;
    }

    // removes all tasks that have not executed from the queue and returns them in a list
    public List<Task> removeAllTasks(){
        // holds all the tasks to return
        List<Task> allTasks = new ArrayList<>();

        // collect all tasks from the different queues
        taskQueues.forEach((p, list) -> list.addAll(allTasks));

        // reset taskQueue
        TaskPriority.getOrder().stream().forEach(priority -> taskQueues.put(priority, new ConcurrentLinkedQueue<>()));

        // return task list
        return allTasks;
    }
}