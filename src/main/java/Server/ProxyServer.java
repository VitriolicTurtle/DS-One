package Server;

import Shared.ServerInfo;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class ProxyServer extends Thread implements ProxyServerInterface{

    private Random random = new Random();

    private Registry registry = null;
    private int port;
    private final int numServers = 5;

    private ServerInterface[] servers = new ServerInterface[numServers];
    private int[] serverPorts = new int[numServers];

    private int[] serverQueues = new int[numServers];

    /**
     * Constructor for the proxy-server.
     * @param port: the port that the proxy-server runs on.
     */
    public ProxyServer(int port) {
        this.port = port;
        startProxyServer();
    }

    /**
     * Exports the proxy-server to the registry.
     * Finds the registry and uses the registry to lookup and store references to the servers.
     */
    private void startProxyServer() {
        try {
            // Export the proxy-server
            UnicastRemoteObject.exportObject(this, port);

            // Get the registry
            registry = LocateRegistry.getRegistry("localhost", 1099);

            // Lookup the 5 processing servers
            for (int i = 0; i < 5; i++) {
                servers[i] = (ServerInterface) registry.lookup("server_" + i);
            }
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start proxy-server.");
            System.exit(1);
        }
        System.out.println("proxy-server has started successfully.");
    }

    /**
     * Fetches the server queue load from the server in the zone provided and updates
     * the serverQueues array.
     * @param zone: the zone in which the server is located.
     * @throws RemoteException
     */
    private void updateServerQueues(int zone) throws RemoteException {
        //TODO: Do this in a thread
        //serverQueues[zone - 1] = servers[zone - 1].getCurrentQueue();
    }

    /**
     * Finds a server for the client to send its query to according to the assignment text.
     * @param zone: the zone in which the client is located.
     * @return ServerInfo used in client.
     * @throws RemoteException
     */
    @Override
    public ServerInfo getServerAssignment(int zone) throws RemoteException {
        if (zone < 0 || zone > 4) {
            System.out.println("\nError:\nInvalid zone number: " + zone + ".");
            System.exit(1);
        }
        //TODO: Code cleanup?

        // Refer client to closest geographically located server if it has capacity
        if (serverQueues[zone] < 10) {
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
        if (serverQueues[neighborServer1] >= 10 && serverQueues[neighborServer2] >= 10) {
            return new ServerInfo("server_" + zone, serverPorts[zone]);
        }

        if (serverQueues[neighborServer1] == serverQueues[neighborServer2]) {
            if (random.nextBoolean()) {
                return new ServerInfo("server_" + neighborServer1, serverPorts[neighborServer1]);
            } else {
                return new ServerInfo("server_" + neighborServer2, serverPorts[neighborServer2]);
            }
        }
        if (serverQueues[neighborServer1] < serverQueues[neighborServer2]) {
            return new ServerInfo("server_" + neighborServer1, serverPorts[neighborServer1]);
        } else {
            return new ServerInfo("server_" + neighborServer2, serverPorts[neighborServer2]);
        }

    }
}
