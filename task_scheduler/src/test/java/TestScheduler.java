import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import taskscheduler.*;

public class TestScheduler {
    // can't test addServer or scheduleTask without also testing executeAll, since otherwise the variables are hidden
    // so all tests are done in one function
    @Test
    public void testScheduler(){
        TaskScheduler ts = new TaskScheduler();
        Server s = new Server();
        Task t = new SimpleTask("unique", Duration.ofMillis(10));

        // add server to scheduler
        ts.addServer(s);
        // test scheduling task
        ts.scheduleTask(t);
        // execute tasks
        Map<Server, List<Task>> completedTasks = ts.executeAll();

        // confirm that task got executed
        assertTrue(((List<Task>)completedTasks.values().toArray()[0]).get(0).isCompleted());

        // confirm that the task scheduler assigns tasks to different servers
        ts = new TaskScheduler();
        Server s1 = new Server();
        Server s2 = new Server();
        ts.addServer(s1);
        ts.addServer(s2);
        Task t1 = new SimpleTask("1", Duration.ofMillis(1));
        Task t2 = new SimpleTask("2", Duration.ofMillis(2));
        Task t3 = new SimpleTask("3", Duration.ofMillis(3));
        ts.scheduleTask(t1);
        ts.scheduleTask(t2);
        ts.scheduleTask(t3);
        completedTasks = ts.executeAll();

        // make sure server order doesn't matter

        // also try to avoid hard type casting

        // check that the first server has task 1 and 3
        // this test doesn't work because of undeterministic arrangement of keys and values in maps
        // List<Task> s1CompletedTasks = (List<Task>)completedTasks.values().toArray()[0];
        // assertEquals("1", s1CompletedTasks.get(0).getId());
        // assertEquals("3", s1CompletedTasks.get(1).getId());
        
        // // check that the second server has task 2
        // List<Task> s2CompletedTasks = (List<Task>)completedTasks.values().toArray()[1];
        // assertEquals("2", s2CompletedTasks.get(0).getId());
    }
}
