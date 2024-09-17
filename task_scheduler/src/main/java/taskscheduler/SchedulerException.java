package taskscheduler;

// exception class for the scheduler
class SchedulerException extends RuntimeException{

    // constructor
    public SchedulerException(String s){
        // call super
        super(s);
    }
}