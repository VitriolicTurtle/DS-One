package Server.ProxyServer;

import Server.ExecutionServer.ExecutionServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class ProxyServer extends Thread implements ProxyServerInterface{
    private Random random = new Random();

    private Registry registry = null;
    private int port;
    private int numServers;

    private ExecutionServerInterface[] servers;
    private int[] serverQueuesSizes;
    private int[] serverAssignmentCounts;

    /**
     * Constructor for the proxy-server.
     * @param port: the port that the proxy-server runs on.
     */
    public ProxyServer(int numServers, int port) {
        this.numServers = numServers;
        this.port = port;

        // Set up arrays to store the server references, the servers' workloads and the counters for how many clients
        // have been assigned to each server (in intervals of 10)
        this.servers = new ExecutionServerInterface[numServers];
        this.serverQueuesSizes = new int[numServers];
        this.serverAssignmentCounts = new int[numServers];

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
            registry = LocateRegistry.getRegistry("localhost", port - 1);

            // Lookup the 5 processing servers
            for (int i = 0; i < numServers; i++)
                servers[i] = (ExecutionServerInterface) registry.lookup("server_" + i);

            // Bind the proxy-server to the registry
            registry.bind("proxy-server", this);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start proxy-server.");
            System.exit(1);
        }
        System.out.println("proxy-server has started successfully.");
    }

    private void updateAssignmentCount(int zone) {
        serverAssignmentCounts[zone]++;
        if (serverAssignmentCounts[zone] >= 10) {
            new Thread(new ProxyServerQueueUpdater(this, zone)).start();
            serverAssignmentCounts[zone] = 0;
        }
    }

    /**
     *
     * @param zone
     * @throws RemoteException
     */
    public void updateQueueData(int zone) throws RemoteException {
        int queueSize = servers[zone].getQueueSize();
        serverQueuesSizes[zone] = queueSize;
    }

    /**
     * Finds a server for the client to send its query to according to the assignment text.
     * @param zone: the zone in which the client is located.
     * @return ServerInfo used in client.
     * @throws RemoteException
     */
    @Override
    public String getServerAssignment(int zone) throws RemoteException {
        if (zone < 0 || zone > 4) {
            System.out.println("\nError:\nInvalid zone number: " + zone + ".");
            System.exit(1);
        }

        // Refer client to closest geographically located server if it has capacity
        if (serverQueuesSizes[zone] < 10)
            return "server_" + zone;

        // If we are referring the client to one of the geographically neighboring servers
        int neighborServer1 = (zone - 1) % numServers;
        int neighborServer2 = (zone + 1) % numServers;
        int selectedServer;

        // If both of the neighboring servers are also at maximum capacity, we refer the user to the closest server
        if (serverQueuesSizes[neighborServer1] >= 10 && serverQueuesSizes[neighborServer2] >= 10)
            selectedServer = zone;

        // If both the neighboring servers have equal workloads to each other, we choose one of them at random
        else if (serverQueuesSizes[neighborServer1] == serverQueuesSizes[neighborServer2])
            selectedServer = (random.nextBoolean()) ? neighborServer1 : neighborServer2;

        // Otherwise we choose the neighboring server with the lowest workload
        else
            selectedServer = (serverQueuesSizes[neighborServer1] < serverQueuesSizes[neighborServer2]) ? neighborServer1 : neighborServer2;

        // Before returning the server address, we update the server's assignment counter
        updateAssignmentCount(zone);

        System.out.println("proxy-server assigned client server: 'server_" + selectedServer + "'.");
        return "server_" + selectedServer;
    }
}