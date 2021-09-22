package Client;

import Server.ProxyServer.ProxyServerInterface;
import Server.ExecutionServer.ExecutionServerInterface;
import Shared.Query.Query;
import Shared.Query.GetTimesPlayedByUserQuery;
import Shared.Query.GetTimesPlayedQuery;
import Shared.Query.GetTopArtistsByUserGenreQuery;
import Shared.Query.GetTopThreeMusicByUserQuery;
import Shared.Response;

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

    private LinkedList<Query> responses;

    private boolean clientCache;
    private ClientCache cache;

    private int sentQueries = 0;
    private int responsesReceived = 0;
    public int expectedQueries = 0;

    private ProxyServerInterface proxyServer = null;
    private ExecutionServerInterface server = null;

    // Used to make sure only one server can send back a response at a time
    Lock lock = new ReentrantLock();

    // Map to store total times for the different query-types
    private HashMap<String, Long> times = new HashMap<String, Long>();

    /**
     * Constructor for client.
     *
     * @param clientNumber: unique ID for the client.
     */
    public Client(int clientNumber, int port, boolean clientCache) {
        this.clientNumber = clientNumber;
        this.responses = new LinkedList<>();

        this.clientCache = clientCache;
        this.cache = new ClientCache(100, 250);

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

    /**
     * Remote method invoked by the server to respond to a query already sent out by the client.
     *
     * @param response: the query object populated with a response.
     * @throws RemoteException
     */
    public void sendQueryResponse(Query response) throws RemoteException {
        lock.lock();

        responsesReceived++;
        acceptResponse(response);
        System.out.println("Client received query response for query: " + response.getHashString() + ". Received responses: " + responsesReceived);

        lock.unlock();
    }

    /**
     *
     * @param response
     */
    private void acceptResponse(Query response) {
        cache.update(response);
        responses.add(response);

        // Set the final event timestamp representing that the query has been returned to the client object
        response.timeStamps[4] = System.currentTimeMillis();
        System.out.println("Query response added. Queries completed: " + responses.size());

        // If we have received the expected number of query responses, we conclude and print the results to file
        if (responses.size() == expectedQueries) {
            conclude();
        }
    }

    /**
     *
     */
    private void conclude() {
        System.out.println("Writing query responses to file ...");
        try {
            //File file = new File("src\\main\\java\\Client\\Outputs\\output.txt"); // WINDOWS
            File file = new File("src/main/java/Client/Outputs/output.txt"); // MAC
            FileWriter writer = new FileWriter(file);

            int numQueriesCompleted = responses.size();

            while (responses.size() != 0) {
                Query response = responses.remove();
                writer.write(response.toString() + "\n");

                // Add the query's turnaround, execution and waiting time to the average statistics
                long turnaround = response.timeStamps[4] - response.timeStamps[0];
                long execution = response.timeStamps[3] - response.timeStamps[2];
                long waiting = response.timeStamps[2] - response.timeStamps[1];

                String turnaroundKey = response.getQueryString() + "Turnaround";
                String executionKey = response.getQueryString() + "Execution";
                String waitingKey = response.getQueryString() + "Waiting";

                times.put(turnaroundKey, times.containsKey(turnaroundKey) ? times.get(turnaroundKey) + turnaround : turnaround);
                times.put(executionKey, times.containsKey(executionKey) ? times.get(executionKey) + execution : execution);
                times.put(waitingKey, times.containsKey(waitingKey) ? times.get(waitingKey) + waiting : waiting);
            }

            // Write the average times to file
            writer.write("\nAverage turnaround time for getTimesPlayedByUser queries: " + times.get("GetTimesPlayedByUserTurnaround") / numQueriesCompleted + "ms\n");
            writer.write("Average execution time for getTimesPlayedByUser queries: " + times.get("GetTimesPlayedByUserExecution") / numQueriesCompleted + "ms\n");
            writer.write("Average waiting time for getTimesPlayedByUser queries: " + times.get("GetTimesPlayedByUserWaiting") / numQueriesCompleted + "ms\n\n");

            writer.write("Average turnaround time for getTimesPlayed queries: " + times.get("GetTimesPlayedTurnaround") / numQueriesCompleted + "ms\n");
            writer.write("Average execution time for getTimesPlayed queries: " + times.get("GetTimesPlayedExecution") / numQueriesCompleted + "ms\n");
            writer.write("Average waiting time for getTimesPlayed queries: " + times.get("GetTimesPlayedWaiting") / numQueriesCompleted + "ms\n\n");

            writer.write("Average turnaround time for getTopArtistsByUserGenre queries: " + times.get("GetTopArtistsByUserGenreTurnaround") / numQueriesCompleted + "ms\n");
            writer.write("Average execution time for getTopArtistsByUserGenre queries: " + times.get("GetTopArtistsByUserGenreExecution") / numQueriesCompleted + "ms\n");
            writer.write("Average waiting time for getTopArtistsByUserGenre queries: " + times.get("GetTopArtistsByUserGenreWaiting") / numQueriesCompleted + "ms\n\n");

            writer.write("Average turnaround time for getTopThreeMusicByUser queries: " + times.get("GetTopThreeMusicByUserTurnaround") / numQueriesCompleted + "ms\n");
            writer.write("Average execution time for getTopThreeMusicByUser queries: " + times.get("GetTopThreeMusicByUserExecution") / numQueriesCompleted + "ms\n");
            writer.write("Average waiting time for getTopThreeMusicByUser queries: " + times.get("GetTopThreeMusicByUserWaiting") / numQueriesCompleted + "ms\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("All query responses have been written to file.");
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

            // Try to client-side cache the query
            boolean cacheHit = false;
            if (clientCache) {
                Response response = fetchCache(query);

                // If the response != null, we have a cache hit
                if (response != null) {
                    query.response = response;
                    cacheHit = true;
                }
            }

            // If we cached the query in the client, we just send the query to the response list
            if (cacheHit) {
                for (int i = 0; i < 5; i++)
                    query.timeStamps[i] = System.currentTimeMillis();

                acceptResponse(query);
            }
            // If we had a cache miss on the client-side, we send the query to the assigned server for processing
            else {
                // Set the timestamp noting the query being sent from the client
                query.timeStamps[0] = System.currentTimeMillis();

                sentQueries++;
                System.out.println("Client sending query: " + query.getHashString() + ". Number of queries sent: " + sentQueries);
                server.sendQuery(query);

            }
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to send query from client_" + clientNumber + " to " + server + ".");
            System.exit(1);
        }
    }

    /**
     *
     * @param query
     * @return
     */
    private Response fetchCache(Query query) {
        return cache.fetch(query);
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