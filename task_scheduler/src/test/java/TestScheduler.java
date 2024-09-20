import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import taskscheduler.*;

public class TestScheduler {
    @Test
    public void testScheduler(){
        TaskScheduler ts = new TaskScheduler();
        Server serv = new Server();
        Task tsk = new SimpleTask("unique", Duration.ofMillis(10));

        // add server to scheduler
        ts.addServer(serv);
        // test scheduling task
        ts.scheduleTask(tsk);
        // execute task
        Map<Server, List<Task>> completedTasks = ts.executeAll();

        // confirm that task got executed  (this line can expand to any number of servers & tasks)
        completedTasks.forEach((s, lt) -> assertTrue(lt.stream().map(Task::isCompleted).reduce(true, (l, r) -> l && r)));

        // confirm that the task scheduler assigns tasks to different servers according to the heuristic
        ts.addServer(new Server());

        // high 1 and 3 should be on the same server, but not the same as high 2
        Task h1 = new PriorityTask("high1", Duration.ofMillis(10), TaskPriority.HIGH);
        Task h2 = new PriorityTask("high2", Duration.ofMillis(12), TaskPriority.HIGH);
        Task h3 = new PriorityTask("high3", Duration.ofMillis(1), TaskPriority.HIGH);

        ts.scheduleTask(h1);
        ts.scheduleTask(h2);
        ts.scheduleTask(h3);

        completedTasks = ts.executeAll();

        completedTasks.forEach((server, taskList) -> {
            switch (taskList.size()) {
                case 2 -> {
                    assertEquals("high1", taskList.get(0).getId());
                    assertEquals("high3", taskList.get(1).getId());
                }
                case 1 -> assertEquals("high2", taskList.get(0).getId());
                default -> assertTrue(false); // neither task list should have any size other than 1 or 2
            }
        });
    }
}
