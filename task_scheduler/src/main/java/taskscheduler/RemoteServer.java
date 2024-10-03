package taskscheduler;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

    // once the number of fails exceeds this number, no more communications will go through to the server for the remainder of the second
    private int circuitBreakerThreshold = 10;
    // keeps track of the number of server failures within the last second
    private int numFails = 0;
    // holds the thread that resets numFails every second
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // runnable function which resets numFails
    private final Runnable periodicReset = new Runnable(){@Override public void run(){resetNumFails();}};
    // this is the thing that actually runs the function every second
    private final ScheduledFuture<?> resetter = scheduler.scheduleAtFixedRate(periodicReset, 0, 1, TimeUnit.SECONDS);

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
    private void bindStub() throws RemoteException {
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
        // check for circuit breaker threshold exceeded, don't even try to schedule
        if(isCircuitBroken())
            throw new ServerException("remote server unresponsive, failed to schedule task");

        // check for null values
        Objects.requireNonNull(task);

        try{
            // send serialized task to remote server
            stub.addTask(task);
        }catch(RemoteException e){
            // increment numFails
            numFails++;

            // throw an error so the scheduler knows the task could not get scheduled
            throw new ServerException("remote server unresponsive, failed to schedule task");
        }
    }

    // executes all the tasks queued in the server
    @Override
    public List<Task> executeTasks() {
        // check for circuit breaker threshold exceeded, don't even try to schedule
        if(isCircuitBroken()){
            // log failure
            LOGGER.severe("server unreachable. executeTasks() failed");

            // return blank list
            return new ArrayList<>();
        }

        try{
            // tell stub to execute all tasks
            return stub.executeTasks();
        }catch(RemoteException e){
            // increment numFails
            numFails++;

            // return blank list (no tasks executed successfully)
            return new ArrayList<>();
        }
    }

    // returns a list of all the failed tasks
    @Override
    public List<Task> getFailedTasks() {
        // check for circuit breaker threshold exceeded, don't even try to schedule
        if(isCircuitBroken()){
            // log failure
            LOGGER.severe("server unreachable. getFailedTasks() failed");

            // return blank list
            return new ArrayList<>();
        }

        try{
            // ask stub for failedTasks list
            return stub.getFailedTasks();
        }catch(RemoteException e){
            // increment numFails
            numFails++;
            
            // return blank list
            return new ArrayList<>();
        }
    }

    // returns a view of ServerMonitor
    @Override
    public ServerStats getStats() {
        // check for circuit breaker threshold exceeded, don't even try to schedule
        if(isCircuitBroken()){
            // log failure
            LOGGER.severe("server unreachable. getStats() failed");

            // return blank list
            return new ServerStats(0, 0, 0, Duration.ofMillis(0));
        }

        try{
            // ask stub for server stats 
            return stub.getStats();
        }catch(RemoteException e){
            // increment fail counter
            numFails++;

            // return blank serverStats
            return new ServerStats(0, 0, 0, Duration.ofMillis(0));
        }
    }

    // returns whether the server is reachable or not
    @Override
    public boolean isOnline(){
        return status;
    }

    // resets the number of fails, this is called every 1 second
    // also tries to reconnect stub if disconnected
    private void resetNumFails(){
        numFails = 0;

        // if server is offline, attempt to reconnect the stub (do nothing with the Exception because we're expecting to still fail)
        if(!status)
            try{bindStub();}catch(RemoteException e){}
        
        System.out.println("reset yes");
    }

    // returns true if numFails exceeds circuitBreakerThreshold, meaning that circuit is currently broken
    public boolean isCircuitBroken(){
        return numFails > circuitBreakerThreshold;
    }
}
