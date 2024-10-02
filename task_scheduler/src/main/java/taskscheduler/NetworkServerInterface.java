package taskscheduler;

import java.rmi.RemoteException;
import java.util.List;
import java.rmi.Remote;

// this is the interface used by the RemoteServer, giving the ServerClient methods to communicate with the remote server
public interface NetworkServerInterface extends Remote {

    // adds task to the server
    public void addTask(Task t) throws RemoteException;

    // executes all the tasks queued in the server
    public List<Task> executeTasks() throws ServerException, RemoteException;

    // returns a list of all the failed tasks
    public List<Task> getFailedTasks() throws RemoteException;

    // returns a view of ServerMonitor
    public ServerStats getStats() throws RemoteException;
}
