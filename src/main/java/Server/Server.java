package Server;

import Shared.Query;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Server implements ServerInterface, Runnable {
    private int serverZone;
    private int port;
    private final String dataFilename = "src\\main\\java\\Server\\Data\\dataset.csv";
    ServerProcessTread processThread;
    ServerQueueThread queueThread;

    /**
     * Constructor for server.
     * @param serverZone: which geographical zone the server is in.
     * @param port: the port the server is running on.
     */
    public Server(Registry registry, int serverZone, int port) {
        this.serverZone = serverZone;
        this.port = port;
        startServer(registry);
    }

    /**
     * Exports the server object to the registry.
     */
    private void startServer(Registry registry) {
        try {
            // Export the server to the registry
            UnicastRemoteObject.exportObject(this, port);

            // Bind the server to the registry
            registry.bind("server_" + serverZone, this);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start server_" + serverZone + ".");
            System.exit(1);
        }
        System.out.println("server_" + serverZone + " has started successfully.");
    }

    /**
     * Main processing thread, handles requests.
     */
    public void startProcessingThread(){
        this.processThread = new ServerProcessTread();
        this.processThread.start();
    }

    /**
     * Processing thread, handles the request queue coming form clients.
     */
    public void startQueueThread(){
        this.queueThread = new ServerQueueThread();
        this.queueThread.start();
    }

    /**
     *
     */
    @Override
    public void run() {

    }

    /**
     * ServerInterface method that allows for clients to send queries for processing.
     * @param query: a query object containing information about which client from which zone has sent the query,
     *             as well as what the query and the query's arguments are.
     * @throws RemoteException
     */
    public void sendQuery(Query query) throws RemoteException {
        System.out.println(query);
    }

    /**
     * Returns the server name as a string.
     * @return
     */
    @Override
    public String toString() {
        return "server_" + serverZone;
    }

}
