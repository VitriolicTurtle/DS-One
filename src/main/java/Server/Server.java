package Server;

import Shared.Query;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Server implements ServerInterface, Runnable {
    private int serverZone;
    private int port;
    private final String dataFilename = "src\\main\\java\\Server\\Data\\dataset.csv";
    ServerProcessTread processThread;
    ServerQueueThread queueThread;

    /**
     * Constructor for server.
     * @param serverZone: which geographical zone the server is in.
     * @param port: the port the server is running on.
     */
    public Server(int serverZone, int port) {
        this.serverZone = serverZone;
        this.port = port;
        startServer();
    }

    /**
     *
     */
    @Override
    public void run() {

    }

    /**
     * Exports the server object to the registry.
     */
    private void startServer() {
        //TODO: bind server to registry
        try {
            // Export the server to the registry
            UnicastRemoteObject.exportObject(this, port);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start server_" + serverZone + ".");
            System.exit(1);
        }
        System.out.println("server_" + serverZone + " has started successfully.");
    }

    /**
     * Main processing thread, handles requests.
     */
    public void startProcessingThread(){
        this.processThread = new ServerProcessTread();
        this.processThread.start();
    }

    /**
     * Processing thread, handles the request queue coming form clients.
     */
    public void startQueueThread(){
        this.queueThread = new ServerQueueThread();
        this.queueThread.start();
    }

    public void sendQuery(Query query) throws RemoteException {
        System.out.println(query);
    }

    /**
     *
     * @param musicID
     * @return
     * @throws RemoteException
     */
    @Override
    public int getTimesPlayed(String musicID) throws RemoteException {
        System.out.println("getTimesPlayed from server_" + serverZone);
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
        System.out.println("getTimesPlayedByUser from server_" + serverZone);
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
        System.out.println("getTimesPlayedByUser from server_" + serverZone);
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
        System.out.println("getTopArtistsByUserGenre from server_" + serverZone);
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
     * Returns the server name as a string.
     * @return
     */
    @Override
    public String toString() {
        return "server_" + serverZone;
    }

}
