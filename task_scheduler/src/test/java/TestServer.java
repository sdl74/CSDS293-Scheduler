import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import taskscheduler.*;

public class TestServer {

    @Test
    public void testServer(){
        Server s = new Server();

        // test with 1 task
        Task t = new SimpleTask("1", Duration.ofMillis(10));
        s.addTask(t);

        // confirm that the server returns a list of completed tasks
        List<Task> completed = s.executeTasks();
        // confirm that the task was returned properly
        assertEquals(1, completed.size());
        // confirm task returned is complete
        assertTrue(completed.get(0).isCompleted());
        // confirm that completed tasks were successfully removed from the task list
        completed = s.executeTasks();
        // no completed tasks
        assertEquals(0, completed.size());

        // now test with multiple tasks
        Task t1 = new SimpleTask("1", Duration.ofMillis(10));
        Task t2 = new SimpleTask("2", Duration.ofMillis(11));
        Task t3 = new SimpleTask("3", Duration.ofMillis(12));
        s.addTask(t1);
        s.addTask(t2);
        s.addTask(t3);
        // confirm that the server returns a list of completed tasks
        completed = s.executeTasks();
        // confirm that the task was returned properly
        assertEquals(3, completed.size());
        // confirm the tasks returned are complete
        assertTrue(completed.get(1).isCompleted());
        // confirm that completed tasks were successfully removed from the queue
        completed = s.executeTasks();
        // no completed tasks
        assertEquals(0, completed.size());

        // test that higher priority tasks finish first
        Task medium = new PriorityTask("medium", Duration.ofMillis(0), TaskPriority.MEDIUM);
        Task low = new PriorityTask("low", Duration.ofMillis(0), TaskPriority.LOW);
        Task high = new PriorityTask("high", Duration.ofMillis(0), TaskPriority.HIGH);
        Task low2 = new PriorityTask("low2", Duration.ofMillis(0), TaskPriority.LOW);
        s.addTask(medium);
        s.addTask(low);
        s.addTask(high);
        s.addTask(low2);

        // completedTasks should return in order of the tasks that were executed, so we can use this to ensure proper task execution order
        completed = s.executeTasks();
        assertEquals("high", completed.get(0).getId());
        assertEquals("medium", completed.get(1).getId());
        String lowId = completed.get(2).getId();
        assertTrue(lowId.equals("low") || lowId.equals("low2"));
    }
}
