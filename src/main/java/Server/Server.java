package main.Server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Server implements ServerInterface {
    private int serverNumber;
    private int port;
    private final String dataFilename = "C:\\Users\\smol\\Desktop\\uni\\in5020\\assignments\\assignment1\\src\\Assignment1\\Server\\Data\\dataset.csv";

    public Server(int serverNumber, int port) {
        this.serverNumber = serverNumber;
        this.port = port;
        startServer();
    }

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

    @Override
    public int getTimesPlayedByUser(String musicID, String userID) throws RemoteException {
        System.out.println("getTimesPlayedByUser from server_" + serverNumber);
        try {
            Scanner scanner = new Scanner(new File(dataFilename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }
        return 0;
    }

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

    @Override
    public int getServerPort() throws RemoteException {
        return port;
    }

    @Override
    public String toString() {
        return "server_" + serverNumber;
    }
}
