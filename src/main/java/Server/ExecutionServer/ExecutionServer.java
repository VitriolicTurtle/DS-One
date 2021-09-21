package Server.ExecutionServer;

import Client.ClientCallbackInterface;
import Shared.*;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ExecutionServer implements ExecutionServerInterface {
    private Registry registry = null;

    public int serverZone;
    private int port;

    private Boolean serverCaching;
    public ExecutionServerCache cache;

    ConcurrentLinkedQueue<Query> queue;

    private final String dataFilename = "src/main/java/Server/Data/dataset.csv"; // MAC

    /**
     * Constructor for server.
     *
     * @param serverZone: which geographical zone the server is in.
     * @param port:       the port the server is running on.
     */
    public ExecutionServer(Registry registry, int serverZone, int port, Boolean serverCaching) {
        this.registry = registry;
        this.serverZone = serverZone;
        this.port = port;
        this.queue = new ConcurrentLinkedQueue<>();
        this.serverCaching = serverCaching;
        this.cache = new ExecutionServerCache(100, 100);

        startServer();
        startProcessingThread();
    }

    /**
     * Exports the server object to the registry.
     */
    private void startServer() {
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

    public Response fetchCache(Query query) {
        return cache.fetch(query);
    }

    /**
     * Fetches a query object from the query queue if the queue isn't empty.
     *
     * @return: a query object, unless the queue is empty in which case null is returned.
     */
    public Query fetchQuery() {
        if (this.queue.size() > 0)
            return queue.remove();
        return null;
    }

    /**
     * Main processing thread. This handles requests as they are added to the queue.
     */
    public void startProcessingThread() {
        new Thread(new ExecutionServerProcessor(this, this.dataFilename, serverCaching)).start();
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
        query.timeStamps[1] = System.currentTimeMillis();
        queue.add(query);
        System.out.println("Query added to server_" + serverZone + " queue. Queue size: " + queue.size());
    }

    /**
     * Respond to a client by sending the query object back. The query object will be populated with the
     * query result.
     * The query object contains all the necessary data to lookup the correct client that originally sent the query,
     * so we know that we return the response to the correct client.
     *
     * @param query: the query (now populated with a query result) being sent to the client.
     */
    public void sendResponse(Query query) {
        try {
            System.out.println("server_" + serverZone + " sending query response to client.");

            // Use the registry to lookup the client that is being responded to
            ClientCallbackInterface client = (ClientCallbackInterface) registry.lookup("client_" + query.getClientNumber());

            // Send the query (that is now populated with a response) back to the client
            cache.update(query);
            client.sendQueryResponse(query);

        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("Something went wrong when responding to client_" + query.getClientNumber() + " from server_" + serverZone);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get the geographical zone of this server.
     *
     * @return: the server zone.
     */
    public int getServerZone() {
        return serverZone;
    }

    /**
     * Returns the server name as a string.
     *
     * @return: the server name.
     */
    @Override
    public String toString() {
        return "server_" + serverZone;
    }
}