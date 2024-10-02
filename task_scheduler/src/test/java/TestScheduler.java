import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import taskscheduler.*;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestScheduler {
    @Test
    public void testScheduler(){
        
    }

    // tests the retry policy
    @Test
    public void testRetryPolicy(){
        // test that tasks get retried the correct number of times
        System.out.println("TEST 1\n");
        // create RetryPolicy with 2 attempts & 0 delay
        RetryPolicy rp = new RetryPolicy(2, new RetryPolicy.UniformTimeout(Duration.ofMillis(0)));
        // create task scheduler with retry policy & server
        TaskScheduler ts = new TaskScheduler(rp);
        ts.addServer(new Server());
        // create task mock which always fails
        SimpleTask t = Mockito.spy(new SimpleTask("A"));
        Mockito.when(t.isCompleted()).thenReturn(false);
        // add & execute task
        ts.scheduleTask(t);
        ts.executeAll();
        // verify task counter
        Mockito.verify(t, Mockito.times(2)).execute();

        // check that retry delay works (other tasks can run while waiting for retry & also when no tasks are left to run, taskScheduler sleeps)
        System.out.println("TEST 2\n");
        // create retryPolicy with 1 retry and 1 second delay
        rp = new RetryPolicy(2, new RetryPolicy.UniformTimeout(Duration.ofMillis(1000)));
        // create task scheduler with retry policy & server
        ts = new TaskScheduler(rp);
        ts.addServer(new Server());
        // task execution process: A runs, B fails, C (depends on A) runs (0.5s), wait 1s since B failed (.5s) then B runs again
        Task A = new SimpleTask("A");// task A is normal. executes in no time
        Task B = Mockito.spy(new SimpleTask("B"));// task B force fail. takes no time
        Mockito.when(B.isCompleted()).thenReturn(false);
        Task C = new TaskFactory("C").dependencies(new String[]{"A"}).duration(500).build();// task C takes .5 seconds
        // track execution time of B.execute()
        final List<LocalTime> destArr = new ArrayList<>();
        Mockito.doAnswer(trackInvocationTime(destArr)).when(B).execute();
        // add & execute tasks
        ts.scheduleTask(A);
        ts.scheduleTask(B);
        ts.scheduleTask(C);
        ts.executeAll();
        // ensure that time between B attempts is > 0.9 & < 1.1 seconds
        assertEquals(1, destArr.get(0).until(destArr.get(1), ChronoUnit.SECONDS), 0.1);
        
        // check that retry delay works (if current batch of tasks takes longer than the retry delay, it will retry after current batch is done)
        System.out.println("TEST 3\n");
        // reset taskScheduler
        ts = new TaskScheduler(rp);
        ts.addServer(new Server());
        // task execution: A runs, B fails (1s delay), C (depends on A) runs (2s), B runs immediately after C
        // A is same as last time
        C = new TaskFactory("C").duration(2000).dependencies(new String[]{"A"}).build();// C takes 2 seconds now
        // reset destArr
        destArr.removeIf(junk -> true);
        // add tasks & execute
        ts.scheduleTask(A);
        ts.scheduleTask(B);
        ts.scheduleTask(C);
        ts.executeAll();
        // ensure that time between B attempts is > 1.9 & < 2.1 seconds
        assertEquals(2, destArr.get(0).until(destArr.get(1), ChronoUnit.SECONDS), 0.1);

        // check that scaling delay works

        // create retryPolicy with 2 attempts and exponential (doubling) penalty
        rp = new RetryPolicy(3, new RetryPolicy.ExponentialTimeout(Duration.ofMillis(1000)));
        // create taskScheduler with retry policy & server
        ts = new TaskScheduler(rp);
        ts.addServer(new Server());
        // task execution: B fails (1s delay), B fails (2s delay), B fails (give up)
        // reset destArr
        destArr.removeIf(junk -> true);
        // add & execute tasks
        ts.scheduleTask(B);
        ts.executeAll();
        // ensure that time between first attempt and second attempt is 0.9 < x < 1.1
        assertEquals(1, destArr.get(0).until(destArr.get(1), ChronoUnit.SECONDS), 0.1);
        // ensure that time between second attempt and third attempt is 1.9 < x < 2.1
        assertEquals(2, destArr.get(1).until(destArr.get(2), ChronoUnit.SECONDS), 0.1);
    }

    // tests scheduling tasks
    @Test
    public void testSchedulingTasks(){
        // ensure that scheduling tasks with different expected runtimes get scheduled correctly (maybe could intercept server with mockito)

        // ensure that when no servers are in the taskscheduler, an exception is thrown

        // ensure that a dependent task gets executed after its dependencies

        // ensure that dependent tasks don't run if their dependencies fail

        // ensure that a dependency loop of tasks never executes

        // ensure that a dependency chain (with more than two tasks) executes in the right order
    }

    // tests remote capabilities
    @Test
    public void testRemoteServers(){
        // generate environment where assigning a task goes to a remote server (but there is a local server in the task scheduler)

        // ensure that when the remote server is down, the task gets scheduled to the local server

        // ensure that if there are no available servers, scheduler throws exception

        // ensure that short circuit pattern works
    }


    // instructions: 
    // final List<LocalTime> destarr = new ArrayList<>();
    // Mockito.doAnswer(trackInvocationTime(destarr)).when(<mock or spy>).<method call>();
    // will fill the destArray with LocalTime instances of every function call <method call>
    // I only sort of understand how this works. copied / modified from https://stackoverflow.com/questions/47552000/verifiying-time-between-calls-with-mockito
    private Answer trackInvocationTime(List<LocalTime> destArray){
        return new Answer(){
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                destArray.add(LocalTime.now());
                return invocationOnMock.callRealMethod();
            }
        };
    }
}
