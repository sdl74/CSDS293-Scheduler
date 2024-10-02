package taskscheduler;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;

// this is the remote server implementation. this runs Server commands over the network using rmi
public class NetworkServer extends UnicastRemoteObject implements NetworkServerInterface {
    // default port: 1900
    private static final int port = 1900;

    // normal server being used to run tasks
    private final Server server = new Server();

    // this method will create a new server instance and bind it to the rmi
    public static void main(String serverName) throws RemoteException {
        // check that the user provided a name
        Objects.requireNonNull(serverName);
        if(serverName.isEmpty()){
            System.out.println("remote server creation failed: server name cannot be blank");
            return;
        }

        // create implementation object
        NetworkServerInterface remoteObject = new NetworkServer();

        // register the remote object with the rmi
        LocateRegistry.createRegistry(port);

        try{
            // bind the remote object with the name provided in args
            Naming.bind("rmi://localhost:" + port + "/" + serverName, remoteObject);
        }catch(MalformedURLException e){
            // notify the user that the name is invalid
            System.out.println("remote server creation failed: name is not valid: " + serverName);
            return;
        }catch(AlreadyBoundException e){
            // notify the user that a server with that name already exists
            System.out.println("remote server creation failed: server with that name already exists");
            return;
        }

        // let user know that remote server creation was successful
        System.out.println("remote server created with name " + serverName);
    }

    public NetworkServer() throws RemoteException {
        // parent constructor
        super();
    }

    // adds task to the server
    @Override
    public void addTask(Task task) throws RemoteException {
        // call server addTask method
        server.addTask(task);
    }

    // executes all the tasks queued in the server
    @Override
    public List<Task> executeTasks() throws ServerException, RemoteException {
        // call server executeTasks method
        return server.executeTasks();
    }

    // returns a list of all the failed tasks
    @Override
    public List<Task> getFailedTasks() throws RemoteException {
        // call server getFailedTasks method
        return server.getFailedTasks();
    }

    // returns a view of ServerMonitor
    @Override
    public ServerStats getStats() throws RemoteException {
        // call server getStats method
        return server.getStats();
    }
}
