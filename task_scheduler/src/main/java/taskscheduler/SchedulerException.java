package taskscheduler;

// exception class for the scheduler
public class SchedulerException extends RuntimeException{

    // constructor
    public SchedulerException(String s){
        // call super
        super(s);
    }
}