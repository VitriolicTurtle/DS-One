package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunServers {
    public static void main(String[] args) {

        //TODO: Split logic statements and main method in too separate methods.
        System.out.println("Starting servers ...");

        Registry registry = null;
        ServerInterface[] servers = new Server[5];
        ProxyServerInterface proxyServer = null;

        try {
            // Create the registry
            registry = LocateRegistry.createRegistry(1099);

            // Start the 5 processing servers and bind them to the registry
            for (int i = 0; i < 5; i++) {
                servers[i] = new Server(i, 1088 + i);
                registry.bind("server_" + i, servers[i]);
            }

            // Start the proxy-server and bind it to the registry
            proxyServer = new ProxyServer(1087);
            registry.bind("proxy-server", proxyServer);

        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong when trying to set up and run servers.");
            System.exit(1);
        }

        System.out.println("All servers have started successfully.");
    }
}
