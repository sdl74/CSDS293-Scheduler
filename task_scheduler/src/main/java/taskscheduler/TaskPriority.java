package taskscheduler;

import java.util.ArrayList;
import java.util.List;

public enum TaskPriority {
    // possible values
    LOW,
    MEDIUM,
    HIGH;

    // constructs and returns a sorted (from high to low) list of the task priorities
    public static List<TaskPriority> getOrder(){
        // create blank list
        List<TaskPriority> sorted = new ArrayList<>();

        // add the priorities
        sorted.add(HIGH);
        sorted.add(MEDIUM);
        sorted.add(LOW);

        // return the list
        return sorted;
    }
}
