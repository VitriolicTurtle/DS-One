package Server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Server implements ServerInterface {
    private int serverNumber;
    private int port;
    private final String dataFilename = "src\\main\\java\\Server\\Data\\dataset.csv";

    /**
     *
     * @param serverNumber
     * @param port
     */
    public Server(int serverNumber, int port) {
        this.serverNumber = serverNumber;
        this.port = port;
        startServer();
    }

    /**
     *
     */
    private void startServer() {
        try {
            // Export the server to the registry
            UnicastRemoteObject.exportObject(this, port);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start server_" + serverNumber + ".");
            System.exit(1);
        }
        System.out.println("server_" + serverNumber + " has started successfully.");
    }

    /**
     *
     * @param musicID
     * @return
     * @throws RemoteException
     */
    @Override
    public int getTimesPlayed(String musicID) throws RemoteException {
        System.out.println("getTimesPlayed from server_" + serverNumber);
        try {
            Scanner scanner = new Scanner(new File(dataFilename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }
        return 0;
    }

    /**
     *
     * @param musicID
     * @param userID
     * @return
     * @throws RemoteException
     */
    @Override
    public int getTimesPlayedByUser(String musicID, String userID) throws RemoteException {
        System.out.println("getTimesPlayedByUser from server_" + serverNumber);
        Scanner scanner = null;
        int counter = 0;

        try {
            scanner = new Scanner(new File(dataFilename));


        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }


        //  Scan trough entire dataset and count amount of times listened to song by userID.
        while (scanner.hasNextLine()) {
            int userIndex = 3;                                            // Smallest index for user is 3 because there is always minimum 1 artist
            String line = scanner.nextLine();
            String[] data = line.split(",");
            while (!data[userIndex].startsWith("U")){                     // If there are more artists than 1, loop through indexes to find user.
                userIndex++;
            }
            if(data[0].equals(musicID) && data[userIndex].equals(userID)) {
                counter+=Integer.parseInt(data[userIndex+1]);
            }
        }
        return counter;
    }

    /**
     *
     * @param userID
     * @return
     * @throws RemoteException
     */
    @Override
    public String[] getTopThreeMusicByUser(String userID) throws RemoteException {
        System.out.println("getTimesPlayedByUser from server_" + serverNumber);
        try {
            Scanner scanner = new Scanner(new File(dataFilename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }
        return new String[0];
    }

    /**
     *
     * @param userID
     * @param genre
     * @return
     * @throws RemoteException
     */
    @Override
    public String[] getTopArtistsByUserGenre(String userID, String genre) throws RemoteException {
        System.out.println("getTopArtistsByUserGenre from server_" + serverNumber);
        try {
            Scanner scanner = new Scanner(new File(dataFilename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }
        return new String[0];
    }

    /**
     *
     * @return
     * @throws RemoteException
     */
    @Override
    public int getServerPort() throws RemoteException {
        return port;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "server_" + serverNumber;
    }
}
