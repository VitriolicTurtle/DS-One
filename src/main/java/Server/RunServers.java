package Server;

import Server.ExecutionServer.ExecutionServer;
import Server.ExecutionServer.ExecutionServerInterface;
import Server.ProxyServer.ProxyServer;
import Server.ProxyServer.ProxyServerInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunServers {
    Registry registry = null;
    ExecutionServerInterface[] servers;
    ProxyServerInterface proxyServer = null;

    /**
     * Method for initializing the proxy-server instance.
     * @param numServers: number of servers that the proxy-server will oversee.
     */
    public void createProxyServer(int numServers, int startPort) {
        // Start the proxy-server and bind it to the registry
        this.proxyServer = new ProxyServer(numServers, startPort + 1);
    }

    /**
     * Method for initializing 5 server instances.
     * @param numServers: number of servers to be initialized.
     */
    public void createServers(int numServers, int startPort, Boolean serverCaching) {
        System.out.println("Starting " + numServers + " servers ...");

        servers = new ExecutionServer[numServers];
        try {
            // Create the registry
            registry = LocateRegistry.createRegistry(startPort);

            // Start the 5 processing servers and bind them to the registry
            for (int i = 0; i < numServers; i++)
                servers[i] = new ExecutionServer(registry, i, startPort + 2 + i, serverCaching);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong when trying to set up and run (processing) servers.");
            System.exit(1);
        }
        System.out.println("All (processing) servers have started successfully.");
    }

    public static void main(String[] args) {
        int numServers = 5;
        Boolean serverCaching = true;

        // ports: registry(startPort), proxy-server(startPort + 1), server0-4(startPort + 2 : startPort + 5), client(startPort + 6)
        // This variable must be identical in RunServers and RunClients
        int startPort = 3197;

        System.out.println("Server caching is " + ((serverCaching) ? "enabled" : "disabled"));
        RunServers startServers = new RunServers();
        startServers.createServers(numServers, startPort, serverCaching);
        startServers.createProxyServer(numServers, startPort);
    }
}
