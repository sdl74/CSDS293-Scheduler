package taskscheduler;

import java.rmi.RemoteException;

// this class shows off the TaskScheduler
public class Main {
    public static void main(String[] args) {
        // //  create a remote server
        // try{
        //     NetworkServer.main("test8D");
        // }catch(RemoteException e){
        //     System.out.println(e.toString());
        // }

        // // simple check for remote server
        // RemoteServer rm = new RemoteServer("test8D");
        // rm.addTask(new SimpleTask("taskId", Duration.ofMillis(1), 10));
        // rm.executeTasks();

        // System.out.println("done");
        oldMain();
    }

    public static void oldMain(){
        // create a TaskScheduler
        TaskScheduler taskScheduler = new TaskScheduler();

        // create two servers & add to the task scheduler
        Server s1 = new Server();
        Server s2 = new Server();
        taskScheduler.addServer(s1);
        taskScheduler.addServer(s2);

        // create a few tasks with varying priority and schedule them
        Task updateSystem = new PriorityTask("update system", Duration.ofMillis(300), 10, TaskPriority.LOW);
        Task checkInternet = new PriorityTask("check internet", Duration.ofMillis(20), 10, TaskPriority.MEDIUM);
        Task userInput = new PriorityTask("user input", Duration.ofMillis(2), 10, TaskPriority.HIGH);
        Task drawCircle = new SimpleTask("draw circle", Duration.ofMillis(10), 10);
        Task sendEmail = new PriorityTask("send email", Duration.ofMillis(30), 10, TaskPriority.LOW);
        Task importantTask = new PriorityTask("important task", Duration.ofMillis(1), 10, TaskPriority.HIGH);

        taskScheduler.scheduleTask(updateSystem);
        taskScheduler.scheduleTask(checkInternet);
        taskScheduler.scheduleTask(userInput);
        taskScheduler.scheduleTask(drawCircle);
        taskScheduler.scheduleTask(sendEmail);
        taskScheduler.scheduleTask(importantTask);

        // execute the tasks (keep in mind that servers execute in series)
        taskScheduler.executeAll();
    }
}
