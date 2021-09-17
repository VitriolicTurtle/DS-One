package Shared;

import java.io.File;
import java.util.*;

/**
 * Class that gives the top 3 artists a specific user ID has listened to based on genre provided based on dataset.csv.
 */
public class GetTopArtistsByUserGenreQuery extends Query {
    // Query arguments
    public String userID;
    public String genre;
    public String[] result;


    /**
     * GetTopArtistsByUser query constructor. The client zone and number of the client sending the query,
     * as well as the arguments for the query, are all determined upon creating the query object.
     *
     * @param clientZone: the zone of the client sending the query.
     * @param clientNumber: the (address) number of the client sending the query.
     * @param userID: the userID argument for the query.
     * @param genre: the genre argument for the query.
     */
    public GetTopArtistsByUserGenreQuery(int clientZone, int clientNumber, String userID, String genre) {
        super(clientZone, clientNumber);
        this.userID = userID;
        this.genre = genre;
        this.cacheKey = "getTopArtistsByUserGenreQuery(" + this.userID + ", " + this.genre + ")";

    }

    @Override
    public void run(String filename) {
        Scanner scanner = null;
        HashMap<String, Integer> playCounts = new HashMap<String, Integer>();

        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(userID) || !line.contains(genre)) { continue; }

            String[] data = line.split(",");

            // Find all artists in the data entry
            for (int i = 1; i < data.length; i++) {
                if (!data[i].startsWith("A")) { break; }

                // Update the play count for the artist found
                if (playCounts.containsKey(data[i])) {
                    playCounts.put(data[i], playCounts.get(data[i]) + 1);
                } else {
                    playCounts.put(data[i], 1);
                }
            }
        }

        String[] topThreeArtists = new String[3];
        for (int i = 0; i < 3; i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            playCounts.remove(topEntry.getKey());
            topThreeArtists[i] = topEntry.getKey();
        }

        result = topThreeArtists;
    }

    public boolean serverCacheRun(List<UserProfile> cachedUsers){
        //System.err.println("TopA");

        return false;
    }

    /*
    public GetTopArtistsByUserGenreResponse cachedRun(List<MusicProfile> cachedMusic, List<UserProfile> cachedUsers){
        //return new GetTopArtistsByUserGenreResponse(zone, clientNumber, result);
        return null;
    }
    */

    @Override
    public String toString() {
        String s = "Top 3 artists for genre '" + genre + "' and user '" + userID + "' were [" + result[0] + ", " + result[1] + ", " + result[2] + "]. ";
        s += "(Turnaround time: " + (timeStamps[4] - timeStamps[0]) + "ms, ";
        s += "execution time: " + (timeStamps[3] - timeStamps[2]) + "ms, ";
        s += "waiting time: " + (timeStamps[2] - timeStamps[1]) + "ms, ";
        s += "processed by server: " + processingServer + ")";
        return s;
    }
}
