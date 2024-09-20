package taskscheduler;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// the server class is a computational node capable of executing tasks
public class Server{
    // holds all tasks to be executed, separated by TaskPriority
    private Map<TaskPriority, ConcurrentLinkedQueue<Task>> taskQueues = new ConcurrentHashMap<>();

    // list of failed tasks
    private List<Task> failedTasks = new ArrayList<>();

    // constructor (doesn't do anything right now)
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
        // list to hold all completed tasks
        List<Task> completedTasks = new ArrayList<>();

        // iterate through each priority level in order
        for(TaskPriority p : TaskPriority.getOrder()){
            // get taskList for easy reference
            AbstractQueue<Task> taskList = taskQueues.get(p);

            // execute tasks
            taskList.stream().forEach(Task::execute);

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
}