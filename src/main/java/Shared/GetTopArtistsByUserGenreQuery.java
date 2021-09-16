package Shared;

import java.io.File;
import java.util.*;

/**
 * Class that gives the top 3 artists a specific user ID has listened to based on genre provided based on dataset.csv.
 */
public class GetTopArtistsByUserGenreQuery extends Query {
    public String userID;
    public String genre;

    public GetTopArtistsByUserGenreQuery(int clientZone, int clientNumber, long sendTime, String userID, String genre) {
        super(clientZone, clientNumber, sendTime);
        this.userID = userID;
        this.genre = genre;
    }

    public GetTopArtistsByUserGenreResponse run(String filename, int serverZone) {
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

        return new GetTopArtistsByUserGenreResponse(clientNumber, clientZone, serverZone, topThreeArtists);
    }

    @Override
    public String toString() {
        return "GetTopArtistsByUserGenreQuery(" + userID + ", " + genre + ") clientZone: " + clientZone;
    }
}
