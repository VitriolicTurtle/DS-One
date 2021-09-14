package Server;

import Shared.Query;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface implemented by the (processing) servers.
 * This contains all the RMI methods available from the (processing) servers.
 */
public interface ServerInterface extends Remote {
    // Method invoked by clients to send query objects for processing
    void sendQuery(Query query) throws RemoteException;
}
