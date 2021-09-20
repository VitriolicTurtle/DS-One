package Shared;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

import Server.Server;

/**
 * Class that gives the top 3 artists a specific user ID has listened to based on genre provided based on dataset.csv.
 */
public class GetTopArtistsByUserGenreQuery extends Query {
    // Query arguments
    public String userID;
    public String genre;

    // Query results
    public String[] result;

    /**
     * GetTopArtistsByUser query constructor. The client zone and number of the client sending the query,
     * as well as the arguments for the query, are all determined upon creating the query object.
     *
     * @param clientZone:   the zone of the client sending the query.
     * @param clientNumber: the (address) number of the client sending the query.
     * @param userID:       the userID argument for the query.
     * @param genre:        the genre argument for the query.
     */
    public GetTopArtistsByUserGenreQuery(int clientZone, int clientNumber, String userID, String genre) {
        super(clientZone, clientNumber);
        this.userID = userID;
        this.genre = genre;
        this.result = new String[3];
    }

    /**
     *
     * @param filename
     * @param server
     */
    @Override
    public void run(String filename, Server server) {
        HashMap<MusicProfile, Integer> playCounts = new HashMap<MusicProfile, Integer>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(userID) || !line.contains(genre))
                continue;

            String[] data = line.split(",");

            // Create a music profile for the music
            ArrayList<String> artists = new ArrayList<>();
            for (int i = 1; i < data.length; i++) {
                if (!data[i].startsWith("A"))
                    break;

                artists.add(data[i]);
            }

            MusicProfile musicProfile = new MusicProfile(data[0], artists);
            int plays = Integer.parseInt(data[data.length - 1]);

            // Add the music profile and its plays
            if (playCounts.containsKey(musicProfile)) {
                playCounts.put(musicProfile, playCounts.get(musicProfile) + plays);
            } else {
                playCounts.put(musicProfile, plays);
            }
        }

        // Find the top three music profiles (based off of play counts)
        MusicProfile[] topThreeProfiles = new MusicProfile[3];
        int[] topThreePlayCounts = new int[3];

        for (int i = 0; i < 3; i++) {
            Map.Entry<MusicProfile, Integer> topEntry = null;
            for (Map.Entry<MusicProfile, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            if (topEntry == null) {
                break;
            }
            playCounts.remove(topEntry.getKey());

            topThreeProfiles[i] = topEntry.getKey();
            topThreePlayCounts[i] = topEntry.getValue();
        }

        System.out.println(topThreeProfiles[0]);
        System.out.println(topThreeProfiles[1]);
        System.out.println(topThreeProfiles[2]);


        // Add the result to the query
        int resultIdx = 0;
        for (int i = 0; i < 3; i++) {
            if (topThreeProfiles[i] == null || resultIdx >= 3)
                break;

            for (String artist : topThreeProfiles[i].artists) {
                result[resultIdx] = artist;
                resultIdx++;

                if (resultIdx >= 3)
                    break;
            }
        }

        // Cache the query result
        server.cacheGetTopArtistsByUserGenre(userID, genre, topThreeProfiles, topThreePlayCounts);
    }

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