package taskscheduler;

// Exception class for server errors
public class ServerException extends RuntimeException {

    // constructor
    public ServerException(String s){
        // call super
        super(s);
    }
}