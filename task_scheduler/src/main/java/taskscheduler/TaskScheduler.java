package taskscheduler;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// this class takes as input Servers and Tasks, distributes tasks to appropriate servers, then can execute all tasks
public class TaskScheduler {
    // maps each priority to a priorityQueue containing ServerWait objects
    private Map<TaskPriority, PriorityQueue<ServerWait>> waitTimes = new EnumMap<>(TaskPriority.class);

    public TaskScheduler(){
        // create blank priorityQueues for each priority in waitTimes
        TaskPriority.getOrder().forEach(p -> waitTimes.put(p, new PriorityQueue<ServerWait>()));
    }

    // adds a server to the list of servers (assumes server has no elements currently in taskQueue for scheduling purposes)
    public synchronized void addServer(Server server){
        // check for null
        if(server == null)
            throw new NullPointerException("cannot add null to server list");

        // defensively copy server
        Server copy = new Server(server);

        // for each TaskPriority, add a new ServerWait entry to the waitTimes
        waitTimes.forEach((priority, serverList) -> serverList.add(new ServerWait(Duration.ofMillis(0), copy)));
    }

    // schedules a task to some available server
    public void scheduleTask(Task task){
        // check for null
        if(task == null)
            throw new NullPointerException("cannot schedule null task");

        // check to make sure there are any servers available
        if(waitTimes.isEmpty())
            throw new SchedulerFullException("no servers are available to schedule to");

        // block so only one thread can modify waitTimes at a time
        synchronized(this){
            // find the server with the shortest wait of this task's priority level
            ServerWait destServer = waitTimes.get(task.getPriority()).poll();

            // add the expected duration of this task to the server wait time
            destServer.expectedWait = destServer.expectedWait.add(task.getEstimatedDuration());

            // add the task to the server
            destServer.server.addTask(task);

            // add the server wait back to the priorityQueue (which sorts it back into the queue)
            waitTimes.get(task.getPriority()).add(destServer);
        }
    }

    // tells all the servers to execute their tasks
    // returns a Map of Servers to their completed task list
    public Map<Server, List<Task>> executeAll() throws SchedulerException{
        // Map to hold return value data
        Map<Server, List<Task>> completedTasks = new HashMap<>();

        // call the execute command for each server and collect the completed tasks in completedTasks
        waitTimes.get(TaskPriority.HIGH).stream().forEach(sw -> completedTasks.put(sw.server, sw.server.executeTasks()));

        // return completedTasks Map
        return completedTasks;
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
}