package Client;

import Server.ProxyServerInterface;
import Server.ServerInterface;
import Shared.GetTimesPlayedQuery;
import Shared.GetTimesPlayedByUserQuery;
import Shared.GetTopThreeMusicByUserQuery;
import Shared.GetTopArtistsByUserGenreQuery;
import Shared.Query;
import Shared.ServerAddress;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client implements Remote, Serializable {
    private int clientNumber;
    private Registry registry = null;

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
            registry = LocateRegistry.getRegistry("localhost", port - 6);

            // Lookup the proxy-server
            proxyServer = (ProxyServerInterface) registry.lookup("proxy-server");

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
     * @param queryString
     * @param zone
     */
    public void processQuery(String queryString, int zone) {
        // Get a server address and port from the proxy server.
        // proxyResponse.address and proxyResponse.port respectively
        ServerAddress proxyResponse = getServerAssignment(zone);

        // Lookup the server we have been referred to by the proxy-server
        try {
            server = (ServerInterface) registry.lookup(proxyResponse.address);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when client_" + clientNumber + " tried to lookup " + proxyResponse.address + ".");
            System.exit(1);
        }

        // Parse the query
        String[] data = queryString.split("\\(");
        String method = data[0];
        String[] arguments = data[1].substring(0, data[1].length() - 1).split(",");

        // Build the query object and send the query to the server
        try {
            Query query = null;
            switch (method) {
                case "getTimesPlayed" -> {
                    assert (arguments.length == 1);
                    query = new GetTimesPlayedQuery(zone, clientNumber, System.currentTimeMillis(), arguments[0]);
                    server.sendQuery(query);
                }
                case "getTimesPlayedByUser" -> {
                    assert (arguments.length == 2);
                    query = new GetTimesPlayedByUserQuery(zone, clientNumber, System.currentTimeMillis(), arguments[0], arguments[1]);
                    server.sendQuery(query);
                }
                case "getTopThreeMusicByUser" -> {
                    assert (arguments.length == 1);
                    query = new GetTopThreeMusicByUserQuery(zone, clientNumber, System.currentTimeMillis(), arguments[0]);
                    server.sendQuery(query);
                }
                case "getTopArtistsByUserGenre" -> {
                    assert (arguments.length == 2);
                    query = new GetTopArtistsByUserGenreQuery(zone, clientNumber, System.currentTimeMillis(), arguments[0], arguments[1]);
                    server.sendQuery(query);
                }
                default -> {
                    System.out.println("\nError:\n");
                    System.out.println("Invalid remote method query: '" + method + "'.");
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to send query from client_" + clientNumber + " to " + server + ".");
            System.exit(1);
        }
    }

    /**
     * Prompts the proxy-server to assign the client a server.
     *
     * @param zone: the zone in which the client is in.
     * @return ServerInfo which contains the address and port for the server assigned by the proxy-server.
     */
    private ServerAddress getServerAssignment(int zone) {
        ServerAddress response = null;
        try {
            response = proxyServer.getServerAssignment(zone);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to get server assignment in client_" + clientNumber + ".");
            System.exit(1);
        }
        return response;
    }
}
