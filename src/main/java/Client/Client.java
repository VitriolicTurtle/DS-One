package Client;

import Server.ProxyServerInterface;
import Server.ServerInterface;
import Shared.GetTimesPlayedQuery;
import Shared.GetTimesPlayedByUserQuery;
import Shared.GetTopThreeMusicByUserQuery;
import Shared.GetTopArtistsByUserGenreQuery;
import Shared.Query;
import Shared.ServerAddress;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

public class Client implements ClientCallbackInterface, Serializable {
    private int clientNumber;
    private Registry registry = null;
    private LinkedList<Query> responses = new LinkedList<>();

    private int expectedResponses = 0;

    private ProxyServerInterface proxyServer = null;
    private ServerInterface server = null;

    /**
     * Constructor for client.
     *
     * @param clientNumber: unique ID for the client.
     */
    public Client(int clientNumber, int port) {
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

    /**
     * Remote method invoked by the server to respond to a query already sent out by the client.
     *
     * @param response: the query object populated with a response.
     * @throws RemoteException
     */
    public void sendQueryResponse(Query response) throws RemoteException {
        // Set the final event timestamp representing that the query has been returned to the client object
        response.timeStamps[4] = System.currentTimeMillis();
        responses.add(response);
        System.out.println(responses.size());
        System.out.println(expectedResponses);

//        expectedResponses--;
    // Jank test.
        if (responses.size() == expectedResponses) {
            writeToFile();
        }
    }

    /**
     * Get a server assignment from the proxy-server, parse the query and build a query object,
     * then send the query object to the server assigned by the proxy-server.
     *
     * @param queryString: the query as a string.
     * @param zone: the zone in which the client is sending the query from.
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

            // Finally, set the timestamp for when the query is sent from the client, then send it to the server
            query.timeStamps[0] = System.currentTimeMillis();
            server.sendQuery(query);
            expectedResponses++;
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to send query from client_" + clientNumber + " to " + server + ".");
            System.exit(1);
        }
    }

    private void writeToFile() {

        try {
            FileWriter writer = new FileWriter("src\\main\\java\\Client\\Outputs\\output_naive.txt");

            while (responses.size() != 0){
                Query response = responses.remove();
                System.out.println(response);
                writer.write(response.toString() + "\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

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
            ServerAddress response = proxyServer.getServerAssignment(zone);

            // Lookup the returned server address
            server = (ServerInterface) registry.lookup(response.address);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to get server assignment in client_" + clientNumber + ".");
            System.exit(1);
        }
    }
}
