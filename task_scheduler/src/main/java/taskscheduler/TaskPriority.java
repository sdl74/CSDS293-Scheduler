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
        List<TaskPriority> sorted = List.of(HIGH, MEDIUM, LOW);

        // return the list
        return sorted;
    }
}
