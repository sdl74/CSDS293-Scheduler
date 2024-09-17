package taskscheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// the server class is a computational node capable of executing tasks
public class Server{
    // list of tasks in the queue
    private List<Task> taskQueue = new ArrayList<>();

    // constructor (doesn't do anything right now)
    public Server(){

    }

    // copy constructor (for defensive copying). creates a new Server that is a copy of s
    public Server(Server s){
        // copy the taskQueue
        this.taskQueue = new ArrayList<>(s.taskQueue);
    }

    // adds a task to the queue
    public void addTask(Task task){
        taskQueue.add(task);
    }

    // executes all tasks in the queue and returns a list of completed tasks
    // all completed tasks will be removed from the queue
    public List<Task> executeTasks() throws ServerException {
        // try catch statement for TaskExceptions
        try {
            // execute task queue as a stream
            taskQueue.stream().forEach(Task::execute);
        } catch (TaskException e) {
            // if any task throws an exception, catch it and throw a ServerException
            throw new ServerException(e.toString());
        }

        // get completed tasks
        List<Task> completedTasks = taskQueue.stream()
            .filter(Task::isCompleted)
            .collect(Collectors.toList());

        // remove completed tasks from the queue
        taskQueue = taskQueue.stream()
            .filter(t -> !t.isCompleted())
            .collect(Collectors.toList());

        // return list of completed tasks
        return completedTasks;
    }

    // returns a list of all the failed tasks
    public List<Task> getFailedTasks(){
        // return all tasks that have not completed
        return taskQueue.stream()
            .filter(t -> !t.isCompleted())
            .collect(Collectors.toList());
    }
}