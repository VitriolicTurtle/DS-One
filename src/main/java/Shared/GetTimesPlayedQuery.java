package Shared;

import java.io.File;
import java.util.Scanner;

import Server.ExecutionServer.ExecutionServer;

/**
 * Class that gives the number of times a musicID has been played in total based on the dataset.csv.
 */
public class GetTimesPlayedQuery extends Query {
    // Query arguments
    public String musicID;

    // Query results
    public int result;

    // Cache variables
    public ArrayList<String> artists;

    /**
     * GetTimesPlayed query constructor. The client zone and number of the client sending the query,
     * as well as the arguments for the query, are all determined upon creating the query object.
     *
     * @param clientZone: the zone of the client sending the query.
     * @param clientNumber: the (address) number of the client sending the query.
     * @param musicID: the musicID argument for the query.
     */
    public GetTimesPlayedQuery(int clientZone, int clientNumber, String musicID) {
        super(clientZone, clientNumber);
        this.musicID = musicID;
    }

    @Override
    public void run(String filename, ExecutionServer server) {
        int counter = 0;
        artists = new ArrayList<>();
        boolean foundArtists = false;

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        //  Scan trough entire dataset and count amount of times listened to song by userID.
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(musicID)) { continue; }

            String[] data = line.split(",");
            counter += Integer.parseInt(data[data.length - 1]);

            if (!foundArtists) {
                for (int i = 1; i < data.length; i++) {
                    if (!data[i].startsWith("A")) { break; }
                    artists.add(data[i]);
                }
                foundArtists = true;
            }
        }
        result = counter;

        // Cache the query result
        server.cacheGetTimesPlayed(musicID, artists, result);

        containsCacheData = true;
    }

    @Override
    public String toString() {
        String s = "Music '" + musicID + "' was played " + result + " times. ";
        s += "(Turnaround time: " + (timeStamps[4] - timeStamps[0]) + "ms, ";
        s += "execution time: " + (timeStamps[3] - timeStamps[2]) + "ms, ";
        s += "waiting time: " + (timeStamps[2] - timeStamps[1]) + "ms, ";
        s += "processed by server: " + processingServer + ")";
        return s;
    }
}