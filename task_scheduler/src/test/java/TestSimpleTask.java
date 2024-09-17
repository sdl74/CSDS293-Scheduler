import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import taskscheduler.Duration;
import taskscheduler.SimpleTask;

public class TestSimpleTask {
    @Test
    public void testGetId(){
        SimpleTask t = new SimpleTask("unique", Duration.ofMillis(10));
        assertEquals("unique", t.getId());
    }

    @Test
    public void testExecute(){
        SimpleTask t = new SimpleTask("unique", Duration.ofMillis(10));
        t.execute();
        // there is nothing to test here because execuete doesn't do anything yet
    }

    @Test
    public void testIsCompleted(){
        SimpleTask t = new SimpleTask("unique", Duration.ofMillis(10));
        // it should start false
        assertFalse(t.isCompleted());

        // after the task has ran, it should be true
        t.execute();
        assertTrue(t.isCompleted());
    }

    @Test
    public void testGetEstimatedDuration(){
        SimpleTask t = new SimpleTask("unique", Duration.ofMillis(10));
        assertEquals(0, Duration.ofMillis(10).compareTo(t.getEstimatedDuration()));
    }
}