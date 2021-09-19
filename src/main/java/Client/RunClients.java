package Client;

import java.io.File;
import java.util.Scanner;

public class RunClients {
    Client client;
    String filename = "src/main/java/Client/Queries/testing_queries.txt";
    Scanner scanner = null;

    /**
     * Sends Queries to servers.
     */
    public void sendQuery() {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split(" ");

            String query = data[0];
            int zone = Integer.parseInt(data[1].substring(5, data[1].length()));

            client.processQuery(query, zone - 1);
        }
    }

    /**
     * Method for creating a client.
     */
    public void runClient(int startPort) {
        System.out.println("Starting client");

        // Create client object
        client = new Client(0, startPort + 7);

        // Create scanner object
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("Something went wrong when trying to create scanner object.");
            System.exit(1);
        }

    }

    /**
     * Main thread for client.
     * @param args runtime arguments.
     */
    public static void main(String[] args) {
        // ports: registry(startPort), proxy-server(startPort + 1), server0-4(startPort + 2 : startPort + 5), client(startPort + 6)
        // This variable must be identical in RunServers and RunClients
        int startPort = 3197;

        RunClients client = new RunClients();
        client.runClient(startPort);
        client.sendQuery();
    }
}
