package Shared.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import Server.ExecutionServer.ExecutionServer;
import Shared.MusicProfile;
import Shared.Response;
import Shared.UserProfile;

/**
 * Class that gives the top 3 musicIDs a users has listened to based on the dataset.csv.
 */
public class GetTopThreeMusicByUserQuery extends Query {
    // Query arguments
    public String userID;

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
    }

    @Override
    public void run(String filename, ExecutionServer server) {
        response = new Response();

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
            MusicProfile musicProfile = new MusicProfile();
            musicProfile.musicID = data[0];
            musicProfile.artists = artists;
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
        }
        response.userProfile = new UserProfile();
        response.userProfile.userID = userID;

        for (int i = 0; i < 3; i++) {
            if (response.userProfile.favoriteMusics.containsKey(topThreeGenres[i])) {
                response.userProfile.favoriteMusics.get(topThreeGenres[i]).add(topThreeProfiles[i]);
            } else {
                response.userProfile.favoriteMusics.put(topThreeGenres[i], new ArrayList<MusicProfile>());
                response.userProfile.favoriteMusics.get(topThreeGenres[i]).add(topThreeProfiles[i]);
            }
        }
    }

    @Override
    public String getQueryString() {
        return "GetTopThreeMusicByUser";
    }

    @Override
    public String getHashString() {
        return "GetTopThreeMusicByUser(" + userID + ")";
    }

    @Override
    public String toString() {
        //String s = "Top 3 musics for user '" + userID + "' were [" + result[0] + ", " + result[1] + ", " + result[2] + "]. ";
        String s = "Top 3 musics for user '" + userID + "' were [";
        int count = 0;

        for (String genre : response.userProfile.favoriteMusics.keySet()) {
            for (MusicProfile musicProfile : response.userProfile.favoriteMusics.get(genre)) {
                for (String artist : musicProfile.artists) {
                    s += artist;
                    count++;
                    if (count < 3)
                        s += ", ";
                }
            }
        }
        while (count < 3) {
            s += "null";
            count++;
            if (count < 3)
                s += ", ";
        }
        s += "]. ";

        s += "(Turnaround time: " + (timeStamps[4] - timeStamps[0]) + "ms, ";
        s += "execution time: " + (timeStamps[3] - timeStamps[2]) + "ms, ";
        s += "waiting time: " + (timeStamps[2] - timeStamps[1]) + "ms, ";
        s += "processed by server: " + processingServer + ")";
        return s;
    }
}