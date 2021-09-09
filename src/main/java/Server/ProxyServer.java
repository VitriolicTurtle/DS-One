package Server;

import Shared.ServerInfo;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class ProxyServer implements ProxyServerInterface {

    private Random random = new Random();

    private Registry registry = null;
    private int port;
    private final int numServers = 5;

    private ServerInterface[] servers = new ServerInterface[numServers];
    private int[] serverPorts = new int[numServers];

    private int[] serverLoads = new int[numServers];

    /**
     *
     * @param port
     */
    public ProxyServer(int port) {
        this.port = port;
        startProxyServer();
    }

    /**
     *
     */
    private void startProxyServer() {
        try {
            // Export the proxy-server
            UnicastRemoteObject.exportObject(this, port);

            // Get the registry
            registry = LocateRegistry.getRegistry("localhost", 1099);

            // Lookup the 5 processing servers
            for (int i = 1; i <= 5; i++) {
                servers[i - 1] = (ServerInterface) registry.lookup("server_" + i);
            }
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start proxy-server.");
            System.exit(1);
        }
        System.out.println("proxy-server has started successfully.");
    }

    /**
     *
     * @param zone
     * @return
     * @throws RemoteException
     */
    @Override
    public ServerInfo getServerAssignment(int zone) throws RemoteException {
        if (zone < 1 || zone > 5) {
            System.out.println("\nError:\nInvalid zone number: " + zone + ".");
            System.exit(1);
        }
        //TODO: Code cleanup?

        // Refer client to closest geographically located server if it has capacity
        if (serverLoads[zone] < 10) {
            return new ServerInfo("server_" + zone, serverPorts[zone]);
        }

        // If we are referring the client to one of the geographically neighboring servers
        int neighborServer1 = zone - 1;
        int neighborServer2 = zone + 1;

        if (neighborServer1 == 6) { neighborServer1 = 1; }
        else if (neighborServer1 == 0) { neighborServer1 = 5; }
        if (neighborServer2 == 6) { neighborServer2 = 1; }
        else if (neighborServer2 == 0) { neighborServer2 = 5; }

        // If both of the neighboring servers are also at maximum capacity, we refer the user to the
        // closest server
        if (serverLoads[neighborServer1] >= 10 && serverLoads[neighborServer2] >= 10) {
            return new ServerInfo("server_" + zone, serverPorts[zone]);
        }

        if (serverLoads[neighborServer1] == serverLoads[neighborServer2]) {
            if (random.nextBoolean()) {
                return new ServerInfo("server_" + neighborServer1, serverPorts[neighborServer1]);
            } else {
                return new ServerInfo("server_" + neighborServer2, serverPorts[neighborServer2]);
            }
        }
        if (serverLoads[neighborServer1] < serverLoads[neighborServer2]) {
            return new ServerInfo("server_" + neighborServer1, serverPorts[neighborServer1]);
        } else {
            return new ServerInfo("server_" + neighborServer2, serverPorts[neighborServer2]);
        }
    }
}
