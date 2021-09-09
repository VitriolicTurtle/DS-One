package Client;

import java.io.File;
import java.util.Scanner;

public class RunClients {
    public static void main(String[] args) {
        //TODO: Split logic statements and main method, in to separate methods.

        System.out.println("Starting clients ...");
        Client client = new Client(0);

        String filename = "src\\main\\java\\Client\\Queries\\queries_naive.txt";

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("Something went wrong when trying to read query file.");
            System.exit(1);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split(" ");

            String query = data[0];
            int zone = Integer.parseInt(data[1].substring(5, data[1].length()));

            client.processQuery(query, zone);
            break;
        }
    }
}
