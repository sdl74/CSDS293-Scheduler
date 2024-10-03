import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import taskscheduler.*;

public class TestPerformanceMonitor {
    
    @Test
    public void testPerformanceMonitor(){
        // make one task fail & one task succeed and then check that performanceMonitor tracks them properly
        // create scheduler & server
        TaskScheduler ts = new TaskScheduler();
        ts.addServer(new Server());
        // create & schedule normal task
        ts.scheduleTask(new SimpleTask("success"));
        // create & schedule task that will fail
        Task fail = Mockito.spy(new SimpleTask("fail"));
        Mockito.when(fail.isCompleted()).thenReturn(false);
        ts.scheduleTask(fail);
        // execute tasks
        ts.executeAll();
        // get reference to performance monitor for less verbose tests
        PerformanceMonitor pm = ts.getStats();
        // check successRate
        assertEquals(0.5, pm.getSuccessRate());
        // add one more successful task and check success rate again
        ts.scheduleTask(new SimpleTask("success2"));
        ts.executeAll();
        pm = ts.getStats();
        assertEquals((float)2/3, pm.getSuccessRate(), .01);

        // ensure averageExecutionTime is correct
        // add a task that takes 400ms to bring the average time to 100ms
        ts.scheduleTask(new TaskFactory("time").duration(400).build());
        ts.executeAll();
        pm = ts.getStats();
        assertEquals(100, pm.getAverageExecutionTime().toMillis(), 10); // give or take a while because other things are happening which contribute to execution time

        // ensure that getServerUtilization is correct
        // create scheduler environment with one server
        ts = new TaskScheduler();
        ts.addServer(new Server());
        // schedule 100ms task
        ts.scheduleTask(new TaskFactory("100ms").estimatedDuration(100).build());
        // add another server
        ts.addServer(new Server());
        // schedule 50ms task
        ts.scheduleTask(new TaskFactory("50ms").estimatedDuration(50).build());
        // execute tasks
        ts.executeAll();
        // confirm utilization is correct
        pm = ts.getStats();
        pm.getServerUtilization();
        double lower = Math.min(pm.getServerUtilization().get(0), pm.getServerUtilization().get(1));
        double higher = Math.max(pm.getServerUtilization().get(0), pm.getServerUtilization().get(1));
        assertEquals((float)2/3, higher);
        assertEquals((float)1/3, lower);
    }

    @Test
    public void testAlertSystem(){
        // test successRate threshold

        // test average duration threshold

        // test max utilization threshold
    }
}
