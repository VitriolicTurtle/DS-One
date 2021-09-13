package Server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunServers {
    Registry registry = null;
    ServerInterface[] servers = new Server[5];
    ProxyServerInterface proxyServer = null;


    /**
     *
     */
    public void CreateProxyServer() {

        // Start the proxy-server and bind it to the registry
        this.proxyServer = new ProxyServer(1087, registry);

    }

    /**
     * Method for initializing 5 server instances.
     *
     * @param amountOfServers Number of Servers to be initialized.
     */
    public void CreateServers(int amountOfServers) {
        System.out.println("Starting " + amountOfServers + " servers ...");
        try {
            // Create the registry
            registry = LocateRegistry.createRegistry(1099);

            // Start the 5 processing servers and bind them to the registry
            for (int i = 0; i < amountOfServers; i++) {
                servers[i] = new Server(i, 1088 + i, registry);
            }


        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong when trying to set up and run servers.");
            System.exit(1);
        }
        System.out.println("All servers have started successfully.");
    }

    public static void main(String[] args) {
        RunServers startServers = new RunServers();
        startServers.CreateServers(5);
        startServers.CreateProxyServer();
    }
}
