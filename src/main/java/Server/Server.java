package Server;

import Shared.Query;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements ServerInterface {
    private int serverZone;
    private int port;

    //ConcurrentHashMap<Client, Query> serverCache = new ConcurrentHashMap<>();
    ConcurrentLinkedQueue<Query> queue = new ConcurrentLinkedQueue<>();
    private final String dataFilename = "src\\main\\java\\Server\\Data\\dataset.csv";

    /**
     * Constructor for server.
     *
     * @param serverZone: which geographical zone the server is in.
     * @param port:       the port the server is running on.
     */
    public Server(Registry registry, int serverZone, int port) {
        this.serverZone = serverZone;
        this.port = port;
        startServer(registry);
        startProcessingThread();
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
     *
     */
    public boolean searchCache(){
        return true;
    }

    /**
     *
     */
    public void addToCache(){

    }

    /**
     * Main processing thread, handles requests.
     */
    public void startProcessingThread() {
        new Thread(new ServerQueryProcessor(this, this.dataFilename, this.serverZone)).start();
    }

    /**
     * ServerInterface method that allows for the proxy-server to get the server's current query queue size.
     *
     * @return int: the current size of the query queue.
     * @throws RemoteException
     */
    public int getQueueSize() throws RemoteException {
        return queue.size();
    }

    /**
     * ServerInterface method that allows for clients to send queries for processing.
     *
     * @param query: a query object containing information about which client from which zone has sent the query,
     *               as well as what the query and the query's arguments are.
     * @throws RemoteException
     */
    public void sendQuery(Query query) throws RemoteException {
        queue.add(query);
    }

    /**
     *
     * @return
     */
    public Query fetchQuery() {
        if (this.queue.size() > 0) {
            return queue.remove();
        }
        return null;
    }

    /**
     * Returns the server name as a string.
     *
     * @return
     */
    @Override
    public String toString() {
        return "server_" + serverZone;
    }

}
