package Client;

import Server.ProxyServer.ProxyServerInterface;
import Server.ExecutionServer.ExecutionServerInterface;
import Shared.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements ClientCallbackInterface, Serializable {
    private int clientNumber;
    private Registry registry = null;

    private boolean clientCache;
    public ClientCache cache = new ClientCache();

    private LinkedList<Query> responses = new LinkedList<>();

    private int sentQueries = 0;
    private int finishedCount = -1;

    private ProxyServerInterface proxyServer = null;
    private ExecutionServerInterface server = null;

    // Used to make sure only one server can send back a response at a time
    Lock lock = new ReentrantLock();

    // Variables to store average times for the different query-types
    long getTimesPlayedByUserTurnaround = 0;
    long getTimesPlayedByUserExecution = 0;
    long getTimesPlayedByUserWaiting = 0;
    long getTimesPlayedTurnaround = 0;
    long getTimesPlayedExecution = 0;
    long getTimesPlayedWaiting = 0;
    long getTopArtistsByUserGenreTurnaround = 0;
    long getTopArtistsByUserGenreExecution = 0;
    long getTopArtistsByUserGenreWaiting = 0;
    long getTopThreeMusicByUserTurnaround = 0;
    long getTopThreeMusicByUserExecution = 0;
    long getTopThreeMusicByUserWaiting = 0;

    /**
     * Constructor for client.
     *
     * @param clientNumber: unique ID for the client.
     */
    public Client(int clientNumber, int port, boolean clientCache) {
        this.clientNumber = clientNumber;
        startClient(port);
    }

    /**
     * Finds and uses the registry to lookup the proxy-server.
     */
    private void startClient(int port) {
        try {
            // Get the registry
            registry = LocateRegistry.getRegistry("localhost", port - 7);

            // Lookup the proxy-server
            proxyServer = (ProxyServerInterface) registry.lookup("proxy-server");

            // Export the client to the registry
            UnicastRemoteObject.exportObject(this, port);

            // Bind the client to the registry
            registry.bind("client_" + clientNumber, this);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start client_" + clientNumber + ".");
            System.exit(1);
        }
        System.out.println("client_" + clientNumber + " has started successfully.");
    }

    public void finished(int count) {
        finishedCount = count;
    }

    /**
     * Remote method invoked by the server to respond to a query already sent out by the client.
     *
     * @param response: the query object populated with a response.
     * @throws RemoteException
     */
    public void sendQueryResponse(Query response) throws RemoteException {
        lock.lock();

        System.out.println("Client received query response.");
        System.out.println("Received responses: " + responses.size());
        acceptResponse(response);

        lock.unlock();
    }

    private void acceptResponse(Query response) {
        responses.add(response);

        // Set the final event timestamp representing that the query has been returned to the client object
        response.timeStamps[4] = System.currentTimeMillis();

        if (finishedCount != -1 && responses.size() == finishedCount) {
            conclude();
        }
    }

    /**
     * Get a server assignment from the proxy-server, parse the query and build a query object,
     * then send the query object to the server assigned by the proxy-server.
     *
     * @param queryString: the query as a string.
     * @param zone:        the zone in which the client is sending the query from.
     */
    public void processQuery(String queryString, int zone) {
        // Get a server assignment from the proxy-server
        getServerAssignment(zone);

        // Parse the query
        String[] data = queryString.split("\\(");
        String method = data[0];
        String[] arguments = data[1].substring(0, data[1].length() - 1).split(",");

        // Build the query object and send the query object to the server for processing
        try {
            Query query = null;
            switch (method) {
                case "getTimesPlayed" -> {
                    assert (arguments.length == 1);
                    query = new GetTimesPlayedQuery(zone, clientNumber, arguments[0]);
                }
                case "getTimesPlayedByUser" -> {
                    assert (arguments.length == 2);
                    query = new GetTimesPlayedByUserQuery(zone, clientNumber, arguments[0], arguments[1]);
                }
                case "getTopThreeMusicByUser" -> {
                    assert (arguments.length == 1);
                    query = new GetTopThreeMusicByUserQuery(zone, clientNumber, arguments[0]);
                }
                case "getTopArtistsByUserGenre" -> {
                    assert (arguments.length == 2);
                    query = new GetTopArtistsByUserGenreQuery(zone, clientNumber, arguments[0], arguments[1]);
                }
                default -> {
                    System.out.println("\nError:\nInvalid remote method query: '" + method + "'.");
                    System.exit(1);
                }
            }

            boolean cacheHit = false;
            if (clientCache) {
                ;
            }

            if (cacheHit) {
                for (int i = 0; i < 5; i++)
                    query.timeStamps[i] = System.currentTimeMillis();

                acceptResponse(query);
            } else {
                // Finally, set the timestamp for when the query is sent from the client, then send it to the server
                query.timeStamps[0] = System.currentTimeMillis();
                server.sendQuery(query);
            }
            sentQueries++;

            System.out.println("Client sent query. Number of sent queries: " + sentQueries);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to send query from client_" + clientNumber + " to " + server + ".");
            System.exit(1);
        }
    }

    private void conclude() {
        System.out.println("Writing query responses to file ...");
        try {
            //File file = new File("src\\main\\java\\Client\\Outputs\\output.txt"); // WINDOWS
            File file = new File("src/main/java/Client/Outputs/output.txt"); // MAC
            FileWriter writer = new FileWriter(file);

            while (responses.size() != 0) {
                Query response = responses.remove();
                writer.write(response.toString() + "\n");

                // Add the query's turnaround, execution and waiting time to the average statistics
                long turnaround = response.timeStamps[4] - response.timeStamps[0];
                long execution = response.timeStamps[3] - response.timeStamps[2];
                long waiting = response.timeStamps[2] - response.timeStamps[1];

                if (response instanceof GetTimesPlayedByUserQuery) {
                    getTimesPlayedByUserTurnaround += turnaround;
                    getTimesPlayedByUserExecution += execution;
                    getTimesPlayedByUserWaiting += waiting;
                } else if (response instanceof GetTimesPlayedQuery) {
                    getTimesPlayedTurnaround += turnaround;
                    getTimesPlayedExecution += execution;
                    getTimesPlayedWaiting += waiting;
                } else if (response instanceof GetTopArtistsByUserGenreQuery) {
                    getTopArtistsByUserGenreTurnaround += turnaround;
                    getTopArtistsByUserGenreExecution += execution;
                    getTopArtistsByUserGenreWaiting += waiting;
                } else if (response instanceof GetTopThreeMusicByUserQuery) {
                    getTopThreeMusicByUserTurnaround += turnaround;
                    getTopThreeMusicByUserExecution += execution;
                    getTopThreeMusicByUserWaiting += waiting;
                }
            }

            // Write the average times to file
            writer.write("\nAverage turnaround time for getTimesPlayedByUser queries: " + getTimesPlayedByUserTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTimesPlayedByUser queries: " + getTimesPlayedByUserExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTimesPlayedByUser queries: " + getTimesPlayedByUserWaiting / sentQueries + "ms\n\n");

            writer.write("Average turnaround time for getTimesPlayed queries: " + getTimesPlayedTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTimesPlayed queries: " + getTimesPlayedExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTimesPlayed queries: " + getTimesPlayedWaiting / sentQueries + "ms\n\n");

            writer.write("Average turnaround time for getTopArtistsByUserGenre queries: " + getTopArtistsByUserGenreTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTopArtistsByUserGenre queries: " + getTopArtistsByUserGenreExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTopArtistsByUserGenre queries: " + getTopArtistsByUserGenreWaiting / sentQueries + "ms\n\n");

            writer.write("Average turnaround time for getTopThreeMusicByUser queries: " + getTopThreeMusicByUserTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTopThreeMusicByUser queries: " + getTopThreeMusicByUserExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTopThreeMusicByUser queries: " + getTopThreeMusicByUserWaiting / sentQueries + "ms\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("All query responses have been written to file.");
    }

    /**
     * Prompts the proxy-server to assign the client a server, then lookups the server address returned
     * from the proxy-server.
     *
     * @param zone: the zone in which the client is in.
     */
    private void getServerAssignment(int zone) {
        try {
            // Ask the proxy-server for a server address
            String address = proxyServer.getServerAssignment(zone);

            // Lookup the returned server address
            server = (ExecutionServerInterface) registry.lookup(address);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to get server assignment in client_" + clientNumber + ".");
            System.exit(1);
        }
    }

}