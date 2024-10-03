import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.Test;

import taskscheduler.*;

import org.mockito.InOrder;
import org.mockito.Mockito;

public class TestServer {

    @Test
    public void testServer(){
        // test that tasks get scheduled correctly by priority
        System.out.println("TEST 1\n");
        // create server
        Server server = new Server();
        // create a task for each priority level (including none)
        Task high = Mockito.spy(new TaskFactory("high").priority("HIGH").build());
        Task medium = Mockito.spy(new TaskFactory("medium").priority("MEDIUM").build());
        Task low = Mockito.spy(new TaskFactory("low").priority("LOW").build());
        Task none = Mockito.spy(new TaskFactory("none").priority("NONE").build());
        // track task execution order
        InOrder inOrder = inOrder(high, medium, low, none);
        // add tasks in pseudo-random order
        server.addTask(medium);
        server.addTask(none);
        server.addTask(high);
        server.addTask(low);
        // execute tasks
        server.executeTasks();
        // ensure that tasks execute in correct order
        inOrder.verify(high).execute();
        inOrder.verify(medium).execute();
        inOrder.verify(low).execute();
        inOrder.verify(none).execute();

        // test that tasks get cancelled if they last too long
        System.out.println("TEST 2\n");
        // create test server
        server = new Server();
        // create, schedule & execute task that will time out (limit is 5 seconds)
        Task A = Mockito.spy(new TaskFactory("saf").duration(10000).build());
        server.addTask(A);
        server.executeTasks();
        // ensure that it is cancelled (check complete)
        assertFalse(A.isCompleted());
        // ensure that cleanup function ran
        Mockito.verify(A, Mockito.times(1)).cleanup();

        // ensure that failed tasks get collected
        System.out.println("TEST 3\n");
        // create test server
        server = new Server();
        // task execution: A, B (fails)
        A = new SimpleTask("A");
        Task B = Mockito.spy(new SimpleTask("B"));
        Mockito.when(B.isCompleted()).thenReturn(false); // ensure B fails
        // schedule tasks & execute
        server.addTask(A);
        server.addTask(B);
        server.executeTasks();
        // ensure that B and only B is in failedTasks
        assertEquals(1, server.getFailedTasks().size());
        assertEquals("B", server.getFailedTasks().get(0).getId());
    }
}
