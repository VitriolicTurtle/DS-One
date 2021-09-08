package java.Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunServers {
    public static void main(String[] args) {
        System.out.println("Starting servers ...");

        Registry registry = null;
        ServerInterface[] servers = new Server[5];
        ProxyServerInterface proxyServer = null;

        try {
            // Create the registry
            registry = LocateRegistry.createRegistry(1099);

            // Start the 5 processing servers and bind them to the registry
            for (int i = 1; i <= 5; i++) {
                servers[i - 1] = new Server(i, 1088 + i);
                registry.bind("server_" + i, servers[i - 1]);
            }

            // Start the proxy-server and bind it to the registry
            proxyServer = new ProxyServer(1088);
            registry.bind("proxy-server", proxyServer);

        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong when trying to set up and run servers.");
            System.exit(1);
        }

        System.out.println("All servers have started successfully.");
    }
}
