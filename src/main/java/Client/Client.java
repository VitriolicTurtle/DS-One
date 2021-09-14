package Client;

import Server.ProxyServerInterface;
import Server.ServerInterface;
import Shared.GetTimesPlayedQuery;
import Shared.GetTimesPlayedByUserQuery;
import Shared.GetTopThreeMusicByUserQuery;
import Shared.GetTopArtistsByUserGenreQuery;
import Shared.Query;
import Shared.ServerInfo;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client implements Remote {

    private int clientNumber;
    private Registry registry = null;

    private ProxyServerInterface proxyServer = null;
    private ServerInterface server = null;

    /**
     * Constructor for client.
     *
     * @param clientNumber: unique ID for the client.
     */
    public Client(int clientNumber) {
        this.clientNumber = clientNumber;
        startClient();
    }

    /**
     * Finds and uses the registry to lookup the proxy-server.
     */
    private void startClient() {
        try {
            //TODO: bind client to the registry
            // Get the registry
            registry = LocateRegistry.getRegistry("localhost", 1099);

            // Lookup the proxy-server
            proxyServer = (ProxyServerInterface) registry.lookup("proxy-server");
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
        //TODO: code cleanup
        System.out.println("Processing query: '" + queryString + "', from zone: " + zone + ".");

        // Get a server address and port from the proxy server.
        // proxyResponse.address and proxyResponse.port respectively
        ServerInfo proxyResponse = getServerAssignment(zone);
        System.out.println("Server assigned by proxy-server: '" + proxyResponse.address + "'.");

        // Lookup the server we have been referred to by the proxy-server
        try {
            server = (ServerInterface) registry.lookup(proxyResponse.address);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when client_" + clientNumber + " tried to lookup " + proxyResponse.address + ".");
            System.exit(1);
        }
        System.out.println("Server " + proxyResponse.address + " assigned.");

        // getTimesPlayedByUser(MghDT6bdDT,UFmWNV9BD0)
        // Parse the query
        String[] data = queryString.split("\\(");
        String method = data[0];
        String[] arguments = data[1].substring(0, data[1].length() - 1).split(",");

        // Invoke the method call
        try {
            Query query = null;

            switch (method) {
                case "getTimesPlayed" -> {
                    assert (arguments.length == 1);
                    //getTimesPlayed(arguments[0]);
                    query = new GetTimesPlayedQuery(zone, arguments[0]);
                    server.sendQuery(query);
                }
                case "getTimesPlayedByUser" -> {
                    assert (arguments.length == 2);
                    query = new GetTimesPlayedByUserQuery(zone, arguments[0], arguments[1]);
                    server.sendQuery(query);
                }
                case "getTopThreeMusicByUser" -> {
                    assert (arguments.length == 1);
                    query = new GetTopThreeMusicByUserQuery(zone, arguments[0]);
                    server.sendQuery(query);
                }
                case "getTopArtistsByUserGenre" -> {
                    assert (arguments.length == 2);
                    query = new GetTopArtistsByUserGenreQuery(zone, arguments[0], arguments[1]);
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
    private ServerInfo getServerAssignment(int zone) {
        ServerInfo response = null;
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
