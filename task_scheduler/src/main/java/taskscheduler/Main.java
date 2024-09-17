package taskscheduler;

import java.util.List;
import java.util.Map;

// this class shows off the TaskScheduler
public class Main {
    public static void main(String[] args) {
        // create a TaskScheduler
        TaskScheduler taskScheduler = new TaskScheduler();

        // create two servers & add to the task scheduler
        Server s1 = new Server();
        Server s2 = new Server();
        taskScheduler.addServer(s1);
        taskScheduler.addServer(s2);

        // create 3 tasks and schedule them with the task scheduler
        Task t1 = new SimpleTask("Task 1", Duration.ofMillis(3));
        Task t2 = new SimpleTask("Task 2", Duration.ofMillis(17));
        Task t3 = new SimpleTask("Task 3", Duration.ofMillis(6));

        taskScheduler.scheduleTask(t1);
        taskScheduler.scheduleTask(t2);
        taskScheduler.scheduleTask(t3);

        // execute the tasks
        Map<Server, List<Task>> completedTasks = taskScheduler.executeAll();

        // print out the ids of each completed task
        completedTasks.forEach((k, v) -> {
            System.out.println("Server Tasks: ");
            ((List<Task>)v).forEach(t -> System.out.println("completed " + t.getId()));
        });
    }
}
