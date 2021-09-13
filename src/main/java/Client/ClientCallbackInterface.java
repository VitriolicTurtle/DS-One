package Client;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import Shared.Response;

public interface ClientCallbackInterface extends Remote {
    void sendResponse(Response response) throws RemoteException;
}
