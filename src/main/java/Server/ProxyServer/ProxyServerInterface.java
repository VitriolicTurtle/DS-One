package Server.ProxyServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface implemented by the proxy-server.
 * This contains all the RMI methods available from the proxy-server.
 */
public interface ProxyServerInterface extends Remote {
    // Method invoked by the client objects to get an address and port for a (processing) server
    String getServerAssignment(int zone) throws RemoteException;
}