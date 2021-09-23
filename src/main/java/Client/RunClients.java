package Client;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

public class RunClients {
    Client client = null;

    // LinkedLists to store the query strings along with their respective zones
    LinkedList<String> queries = new LinkedList<>();
    LinkedList<Integer> zones = new LinkedList<>();

    /**
     * Method for creating the client object.
     */
    public void runClient(int startPort, boolean clientCache, String outputFilename) {
        client = new Client(0, startPort + 7, clientCache, outputFilename);
    }

    public void readQueryFile(String filename) {
        // Create scanner object
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("Something went wrong when trying to create scanner object.");
            System.exit(1);
        }

        // Read all the queries and store them along with their respective zones
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split(" ");

            String query = data[0];
            int zone = Integer.parseInt(data[1].substring(5)) - 1;

            queries.add(query);
            zones.add(zone);
        }

        // Tell the client object how many queries are going to be processed.
        // This way we know how many query responses to expect
        client.expectedQueries = queries.size();
    }

    /**
     * Sends all the queries for processing.
     */
    public void sendQueries() {
        // Count how many queries are sent since we pause the sending of queries for 500ms every 10 queries
        int count = 0;

        while (queries.size() > 0) {
            String query = queries.removeFirst();
            int zone = zones.removeFirst();

            client.processQuery(query, zone);
            count++;

            // Pause for 500ms if count % 10 == 0
            if (count % 10 == 0) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Main thread for client.
     *
     * @param args runtime arguments.
     */
    public static void main(String[] args) {
        // ports: registry(startPort), proxy-server(startPort + 1), server0-4(startPort + 2 : startPort + 5), client(startPort + 6)
        // This variable must be identical in RunServers and RunClients
        int startPort = 2400;

        // The input filename
        String inputFilename = args[0];
        String outputFilename = args[1];
        boolean clientCache = Boolean.parseBoolean(args[2]);

        //String inputFilename = "src/main/java/Client/Queries/cached_input.txt";
        //String outputFilename = "src/main/java/Client/Outputs/output.txt";
        //boolean clientCache = true;

        System.out.println("Starting client ...");
        RunClients client = new RunClients();

        client.runClient(startPort, clientCache, outputFilename);
        client.readQueryFile(inputFilename);
        client.sendQueries();
    }
}
