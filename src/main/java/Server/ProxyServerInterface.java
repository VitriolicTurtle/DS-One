package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Shared.ServerInfo;

/**
 * Interface implemented by the proxy-server.
 * Contains all the RMI methods available from the proxy-server.
 */
public interface ProxyServerInterface extends Remote {
    // Method to get the address and port number of an available server
    ServerInfo getServerAssignment(int zone) throws RemoteException;
}
