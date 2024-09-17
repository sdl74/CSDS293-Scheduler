package taskscheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// the server class is a computational node capable of executing tasks
public class Server{
    // list of tasks in the queue
    private List<Task> taskQueue = new ArrayList<>();

    // list of failed tasks
    private List<Task> failedTasks = new ArrayList<>();

    // constructor (doesn't do anything right now)
    public Server(){

    }

    // copy constructor (for defensive copying). creates a new Server that is a copy of s
    public Server(Server s){
        // check for null
        if(s == null)
            throw new NullPointerException("cannot create a copy of a null server");

        // copy the taskQueue
        this.taskQueue = new ArrayList<>(s.taskQueue);
    }

    // adds a task to the queue
    public void addTask(Task task){
        // check for null task
        if(task == null)
            throw new NullPointerException("cannot add null task to the queue");

        // add the task to the queue
        taskQueue.add(task);
    }

    // executes all tasks in the queue and returns a list of completed tasks
    // all completed tasks will be removed from the queue
    public List<Task> executeTasks() throws ServerException {
        // execute task queue as a stream
        taskQueue.stream().forEach(Task::execute);

        // get completed tasks
        List<Task> completedTasks = taskQueue.stream()
            .filter(Task::isCompleted)
            .collect(Collectors.toList());

        // remove completed tasks from the queue
        taskQueue = taskQueue.stream()
            .filter(t -> !t.isCompleted())
            .collect(Collectors.toList());

        // all tasks that are left on the queue must have failed, so they are moved to the failedTasks list
        failedTasks = taskQueue;

        // clear taskQueue
        taskQueue = new ArrayList<>();

        // return list of completed tasks
        return completedTasks;
    }

    // returns a list of all the failed tasks
    public List<Task> getFailedTasks(){
        // make defensive copy and return failedTask list
        return new ArrayList<>(failedTasks);
    }
}