package taskscheduler;

import java.util.List;

public enum TaskPriority {
    // possible values
    NONE,
    LOW,
    MEDIUM,
    HIGH;

    // constructs and returns a sorted (from high to low) list of the task priorities
    public static List<TaskPriority> getOrder(){
        // create blank list
        List<TaskPriority> sorted = List.of(HIGH, MEDIUM, LOW, NONE);

        // return the list
        return sorted;
    }
}
