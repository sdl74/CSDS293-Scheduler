package taskscheduler;

// custom exception class for tasks (must be unchecked to be compatable with streams)
class TaskException extends RuntimeException {

    // constructor simply calls super
    public TaskException(String s){
        // call super
        super(s);
    }
}