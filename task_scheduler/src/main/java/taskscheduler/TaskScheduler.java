package taskscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this class takes as input Servers and Tasks, distributes tasks to appropriate servers, then can execute all tasks
public class TaskScheduler {
    // list of servers available
    private List<Server> servers = new ArrayList<>();

    // roatating index, so incoming tasks get distributed evenly across servers
    private int nextServer = 0;

    // adds a server to the list of servers
    public void addServer(Server server){
        // check for null
        if(server == null)
            throw new NullPointerException("cannot add null to server list");

        // copy server then add it to serverList
        servers.add(new Server(server));
    }

    // schedules a task to some available server
    public void scheduleTask(Task task){
        // check for null
        if(task == null)
            throw new NullPointerException("cannot schedule null task");

        // assign task to server if any are available
        if(!servers.isEmpty())
            servers.get(nextServer).addTask(task);
        else{
            // no servers are available to schedule the task, throw error
            throw new SchedulerException("no servers are available to schedule to");
        }

        // increment server index and check for bound
        nextServer++;
        if(nextServer >= servers.size())
            nextServer = 0;
    }

    // tells all the servers to execute their tasks
    // returns a Map of Servers to their completed task list
    public Map<Server, List<Task>> executeAll() throws SchedulerException{
        // Map to hold return value data
        Map<Server, List<Task>> completedTasks = new HashMap<>();

        // call the execute command for each server and collect the completed tasks in completedTasks
        servers.stream().forEach(s -> completedTasks.put(s, s.executeTasks()));

        // return completedTasks Map
        return completedTasks;
    }
}