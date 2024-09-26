package taskscheduler;

import java.util.HashSet;
import java.util.Set;

public class DependentTask extends PriorityTask {

    // dependency set
    private final Set<String> dependencies;

    // constructor
    public DependentTask(String newId, Duration newDuration, long realTime, TaskPriority newPriority, Set<String> newDependencies) {
        // call super
        super(newId, newDuration, realTime, newPriority);

        // check for null values
        if(newDependencies == null)
            throw new NullPointerException("dependency list cannot be null");
        
        // create copy of newDependencies
        dependencies = new HashSet<>(newDependencies);

        // check for null values within dependencies (done after copy since some implementations of Set will throw exception when checking for contains null but HashSet does not)
        if(dependencies.contains(null))
            throw new NullPointerException("dependency id cannot be null");
    }
    
    // return the list of dependencies, copied for protection
    @Override
    public Set<String> getDependencies(){
        return new HashSet<>(dependencies);
    }
}
