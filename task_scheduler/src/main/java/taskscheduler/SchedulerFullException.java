package taskscheduler;

// exception class for when the scheduler has no servers available
class SchedulerFullException extends SchedulerException{

    // constructor
    public SchedulerFullException(String s){
        // call super
        super(s);
    }
}