import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import taskscheduler.*;

public class TestServer {
    @Test
    public void testAddTask(){
        Server s = new Server();
        Task t = new SimpleTask("unique", Duration.ofMillis(10));

        // confirm that there are no failed tasks (failed ~= not completed)
        assertEquals(0, s.getFailedTasks().size());

        // add task to server then check for "failed" tasks
        s.addTask(t);
        assertEquals(1, s.getFailedTasks().size());

        // test adding more tasks
        Task t2 = new SimpleTask("unique2", Duration.ofMillis(11));
        Task t3 = new SimpleTask("unique3", Duration.ofMillis(12));
        s.addTask(t2);
        s.addTask(t3);

        // check length of incomplete tasks
        assertEquals(3, s.getFailedTasks().size());
    }

    @Test
    public void testExecuteTasks(){
        Server s = new Server();

        // test with 1 task
        Task t = new SimpleTask("unique", Duration.ofMillis(10));
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
        Task t1 = new SimpleTask("unique1", Duration.ofMillis(10));
        Task t2 = new SimpleTask("unique2", Duration.ofMillis(11));
        Task t3 = new SimpleTask("unique3", Duration.ofMillis(12));
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
    }

    @Test
    public void testGetFailedTasks(){
        // there is no way to test this currently because every task will succeed
    }
}
