import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import taskscheduler.Duration;

public class TestDuration{
    @Test
    public void testComparison(){
        // test equals
        Duration d1 = Duration.ofMillis(10);
        Duration d2 = Duration.ofMillis(10);
        assertEquals(0, d1.compareTo(d2));

        // test greater than
        d2 = Duration.ofMillis(2);
        assertEquals(1, d1.compareTo(d2));

        // test less than
        assertEquals(-1, d2.compareTo(d1));
    }

    @Test
    public void testAddition(){
        Duration d1 = Duration.ofMillis(10);
        Duration d2 = Duration.ofMillis(2);
        Duration result = Duration.ofMillis(12);
        assertEquals(0, result.compareTo(d1.add(d2)));
    }

    @Test
    public void testSubtraction(){
        Duration d1 = Duration.ofMillis(10);
        Duration d2 = Duration.ofMillis(2);
        Duration result = Duration.ofMillis(8);
        assertEquals(0, result.compareTo(d1.subtract(d2)));
    }
}