package taskscheduler;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

// RemoteServer communicates with a NetworkServer over RMI to allow remote execution of tasks
// this is the client in the rmi trifecta of classes (remote object interface, remote object implementation & client)
public class RemoteServer extends Server {
    // logger
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    // port (default is 1900)
    private static final String port = "1900";

    // remote object name
    private final String id;

    // reference to the RemoteObject
    NetworkServerInterface stub;

    // server status. When true, server is responsive, when false, server is unresponsive
    private boolean status = false;

    // when rmi does not respond, whatever function that was called will automatically re-call itself after attempting to re-bind for maxRetryAttempts amount of times
    private final int maxRetryAttempts = 1;
    private int retryAttemptsLeft = maxRetryAttempts;

    // list of unexecuted tasks sent out to the remote server (important so that they are not lost if server goes offline before execution happens)
    private List<Task> queuedTasks = new ArrayList<>();
    // when this flag is true, removeAllTasks() will be called to the remote server as soon as it comes back online
    private boolean flushTasksFlag = false;

    // when you create a new RemoteServer, you need to pass the id of a remote object that should already be bound to the rmi registry
    RemoteServer(String objectName){
        // call parent constructor
        super();

        // check for null
        Objects.requireNonNull(objectName);

        // set object name
        id = objectName;

        // attempt to bind stub to a remote objecton the rmi registry
        try{
            bindStub();
        }catch(RemoteException e){
            // wrap the RemoteException in an Exception class that can pass through the Server parent class
            throw new ServerException(e);
        }

        // check for failure and throw ServerException if stub could not be bound
        if(!status)
            throw new ServerException("remote server could not be made. check logger for more details");
    }

    // searches the rmi registry for a remote object with the name id (using the variable id)
    private void bindStub()  throws RemoteException {
        // initialize status flag to false (if status = true doesn't run later in the method, some error occured and server is assumed unresponsive for whatever reason)
        status = false;

        try{
            // search for the remote object on the rmi registry
            stub = (NetworkServerInterface)Naming.lookup("rmi://localhost:" + port + "/" + id);

            // change status to indicate that server is reachable
            status = true;

        }catch(NotBoundException e){// happens when no stub found with this id
            LOGGER.log(Level.SEVERE, "remote server {0} not found. Make sure name is correct and server is online", id);
            throw new IllegalArgumentException(e);
        }catch(MalformedURLException e){// thrown if there's a formatting error in the id
            // throw illegal argument exception, letting the user know the name is invalid
            throw new IllegalArgumentException("remote server name (" + id + ") is illegal, please rename server");
        }catch(RemoteException e){
            // log the exception as a warning then throw it back
            LOGGER.log(Level.WARNING, e.toString());
            throw e;
        }
    }

    // adds a task to the server
    @Override
    public void addTask(Task task){
        // check for null values
        Objects.requireNonNull(task);

        try{
            // if need to flush tasks, attempt to flush tasks (needs to happen before adding a task so new task doesn't get flushed later)
            if(flushTasksFlag){
                stub.removeAllTasks();
                flushTasksFlag = false;
            }

            // send serialized task to remote server
            stub.addTask(task);
        }catch(RemoteException e){
            // attempt to re-connnect to stub (error is only logged because it will be thrown after retry fails)
            try{bindStub();}catch(RemoteException re){}

            // if binding was successful& can still retry, retry addTask
            if(status && retryAttemptsLeft > 0){
                retryAttemptsLeft--;
                addTask(task); // recursively attempt to addTask
                return; // return so that if later attempt is successful, exception doesn't get thrown
            }

            // reset attempt counter once reaches 0 / successful recursion
            retryAttemptsLeft = maxRetryAttempts;

            // wrap to a ServerException and throw to let TaskScheduler know that task did not get scheduled
            throw new ServerException(e);
        }

        // add task to queuedTasks
        queuedTasks.add(task);
    }

    // executes all the tasks queued in the server
    @Override
    public List<Task> executeTasks() {
        try{
            // if need to flush tasks, attempt to flush tasks (needs to happen before execution so flushed tasks don't get executed)
            if(flushTasksFlag){
                stub.removeAllTasks();
                flushTasksFlag = false;
            }

            // tell stub to execute all tasks
            List<Task> successfulTasks = stub.executeTasks();

            // if succeed, clear queuedTasks
            queuedTasks = new ArrayList<>();

            // return successful tasks
            return successfulTasks;
        }catch(RemoteException e){
            // attempt to re-connect to stub (error is only logged because it will be thrown after retry fails)
            try{bindStub();}catch(RemoteException re){}

            // if binding was successful & can retry, attempt to communicate again
            if(status && retryAttemptsLeft > 0){
                retryAttemptsLeft--;
                return executeTasks();
            }

            // reset attempt counter once reaches 0 / successful recursion
            retryAttemptsLeft = maxRetryAttempts;

            // otherwise, wrap to ServerException and throw to scheduler
            throw new ServerException(e);
        }
    }

    // returns a list of all the failed tasks
    @Override
    public List<Task> getFailedTasks() {
        try{
            // ask stub for failedTasks list
            return stub.getFailedTasks();
        }catch(RemoteException e){
            // attempt to re-connect to stub (error is only logged because it will be thrown after retry fails)
            try{bindStub();}catch(RemoteException re){}

            // if binding was successful & can retry, attempt to communicate again
            if(status && retryAttemptsLeft > 0){
                retryAttemptsLeft--;
                return getFailedTasks();
            }

            // reset attempt counter once reaches 0 / successful recursion
            retryAttemptsLeft = maxRetryAttempts;

            // otherwise, wrap to ServerException and throw to scheduler
            throw new ServerException(e);
        }
    }

    // returns a view of ServerMonitor
    @Override
    public ServerStats getStats() {
        try{
            // ask stub for server stats 
            return stub.getStats();
        }catch(RemoteException e){
            // attempt to re-connect to stub (error is only logged because it will be thrown after retry fails)
            try{bindStub();}catch(RemoteException re){}

            // if binding was successful & can retry, attempt to communicate again
            if(status && retryAttemptsLeft > 0){
                retryAttemptsLeft--;
                return getStats();
            }

            // reset attempt counter once reaches 0 / successful recursion
            retryAttemptsLeft = maxRetryAttempts;

            // otherwise, wrap to ServerException and throw to scheduler
            throw new ServerException(e);
        }
    }

    // returns whether the server is reachable or not
    @Override
    public boolean isOnline(){
        // if the last attempt to reach the server failed, attempt to re-bind to stub
        if(!status) // error is discarded because the user uses isOnline() to avoid contacting an offline server & causing an error
            try{bindStub();}catch(RemoteException e){}
        
        // return status variable
        return status;
    }

    // returns all tasks that have not executed yet (kept in list) and tells the server to dequeue all its tasks (or sets a flag to do it later when the server comes back online)
    @Override
    public List<Task> removeAllTasks(){
        try{
            // try to remove all tasks
            List<Task> retVal = stub.removeAllTasks();

            // if succeed, forget queuedTasks and return retVal
            queuedTasks = new ArrayList<>();

            return retVal;
        }catch(RemoteException e){
            // if the stub fails to respond, set the flag to remove all tasks when the remote server comes back online
            flushTasksFlag = true;

            // then return the list of saved tasks
            return queuedTasks;
        }
    }
}
