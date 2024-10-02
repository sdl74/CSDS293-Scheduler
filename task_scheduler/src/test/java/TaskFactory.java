import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import taskscheduler.*;

// for simple creation of tasks when testing
public class TaskFactory {
    // values
    private String taskId;
    private TaskPriority priorityVal = TaskPriority.NONE;
    private Set<String> dependenciesVal = new HashSet<>();
    private Duration estDurationVal = Duration.ofMillis(0);
    private long durationVal = 0;

    public TaskFactory(String taskid){
        taskId = taskid;
    }

    // set the priority and return this (uses string for less verbose input)
    public TaskFactory priority(String p){
        // check for possible values (if invalid assume no priority)
        switch (p) {
            case "HIGH" -> priorityVal = TaskPriority.HIGH;
            case "MEDIUM" -> priorityVal = TaskPriority.MEDIUM;
            case "LOW" -> priorityVal = TaskPriority.LOW;
        }

        // return this
        return this;
    }

    // set dependencies using normal String array
    public TaskFactory dependencies(String[] dep){
        // add dependencies
        dependenciesVal.addAll(Arrays.asList(dep));

        // return this
        return this;
    }

    // set estimated duration
    public TaskFactory estimatedDuration(long d){
        // set duration
        estDurationVal = Duration.ofMillis(d);

        // return this
        return this;
    }

    // set real duration value
    public TaskFactory duration(long d){
        // set duration
        durationVal = d;

        // return this
        return this;
    }

    // builds a task
    public Task build(){
        // checks if this is a dependentTask
        if(!dependenciesVal.isEmpty())
            return new DependentTask(taskId, estDurationVal, durationVal, priorityVal, dependenciesVal);
        // checks if this is a priorityTask
        if(priorityVal != TaskPriority.NONE)
            return new PriorityTask(taskId, estDurationVal, durationVal, priorityVal);
        // otherwise must be simpleTask
        return new SimpleTask(taskId, estDurationVal, durationVal);
    }
}
