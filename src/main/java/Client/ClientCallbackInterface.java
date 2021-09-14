package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Shared.Response;

/**
 * Interface implemented by the client objects.
 * This contains all the RMI methods available from the client objects.
 */
public interface ClientCallbackInterface extends Remote {
    // Method invoked by the servers to send a query response back to the client
    void sendResponse(Response response) throws RemoteException;
}
