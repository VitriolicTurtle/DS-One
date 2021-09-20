package Shared;

import java.io.File;
import java.util.*;

import Server.Server;

/**
 * Class that gives the top 3 musicIDs a users has listened to based on the dataset.csv.
 */
public class GetTopThreeMusicByUserQuery extends Query {
    // Query arguments
    public String userID;

    // Query results
    public String[] result;

    /**
     * GetTopThreeMusicByUser query constructor. The client zone and number of the client sending the query,
     * as well as the arguments for the query, are all determined upon creating the query object.
     *
     * @param clientZone:   the zone of the client sending the query.
     * @param clientNumber: the (address) number of the client sending the query.
     * @param userID:       the userID argument for the query.
     */
    public GetTopThreeMusicByUserQuery(int clientZone, int clientNumber, String userID) {
        super(clientZone, clientNumber);
        this.userID = userID;
        this.result = new String[3];
    }

    @Override
    public void run(String filename, Server server) {
        LinkedHashMap<MusicProfile, Integer> playCounts = new LinkedHashMap<>();
        LinkedHashMap<MusicProfile, String> genres = new LinkedHashMap<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        // Scan the entire dataset and collect any relevant data
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(userID))
                continue;

            String[] data = line.split(",");

            // Find all the artists for the music
            ArrayList<String> artists = new ArrayList<>();
            for (int i = 1; i < data.length; i++) {
                if (!data[i].startsWith("A"))
                    break;

                artists.add(data[i]);
            }

            // Create a music profile for the music
            MusicProfile musicProfile = new MusicProfile(data[0], artists);
            int plays = Integer.parseInt(data[data.length - 1]);
            String genre = data[data.length - 3];

            // Store the music profile along with its play count and genre
            playCounts.put(musicProfile, plays);
            genres.put(musicProfile, genre);
        }

        MusicProfile[] topThreeProfiles = new MusicProfile[3];
        int[] topThreePlayCounts = new int[3];
        String[] topThreeGenres = new String[3];

        // Find the top three most played musics (in descending order)
        for (int i = 0; i < 3; i++) {
            Map.Entry<MusicProfile, Integer> topEntry = null;
            for (Map.Entry<MusicProfile, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            if (topEntry == null)
                break;

            playCounts.remove(topEntry.getKey());

            topThreeProfiles[i] = topEntry.getKey();
            topThreePlayCounts[i] = topEntry.getValue();
            topThreeGenres[i] = genres.get(topEntry.getKey());

            // Add the found musicID to the result
            result[i] = topEntry.getKey().musicID;
        }

        // Cache the query result
        server.cacheGetTopThreeMusicByUser(userID, topThreeProfiles, topThreePlayCounts, topThreeGenres);
    }

    @Override
    public String toString() {
        String s = "Top 3 musics for user '" + userID + "' were [" + result[0] + ", " + result[1] + ", " + result[2] + "]. ";
        s += "(Turnaround time: " + (timeStamps[4] - timeStamps[0]) + "ms, ";
        s += "execution time: " + (timeStamps[3] - timeStamps[2]) + "ms, ";
        s += "waiting time: " + (timeStamps[2] - timeStamps[1]) + "ms, ";
        s += "processed by server: " + processingServer + ")";
        return s;
    }
}