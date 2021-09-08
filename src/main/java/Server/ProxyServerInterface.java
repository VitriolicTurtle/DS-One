package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Shared.ServerInfo;

public interface ProxyServerInterface extends Remote {
    // Method to get the address and port number of an available server
    ServerInfo getServerAssignment(int zone) throws RemoteException;
}
