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
    }

    /**
     *
     * @param filename
     * @param server
     */
    @Override
    public void run(String filename, Server server) {
        Scanner scanner = null;
        HashMap<String, Integer> playCounts = new HashMap<String, Integer>();

        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        // Hashmaps needed for making cache entry.
        HashMap<String, Integer> musicCounts = new HashMap<>();
        HashMap<String, ArrayList<String>> artists = new HashMap<>();

        //ArrayList<String> musicIDs = new ArrayList<>();
        //ArrayList<Integer> timesPlayed = new ArrayList<>();

        while (scanner.hasNextLine()) {
            ArrayList<String> tempArtistList = new ArrayList<>();

            String line = scanner.nextLine();
            if (!line.contains(userID) || !line.contains(genre)) {
                continue;
            }

            String[] data = line.split(",");

            // Find all artists in the data entry
            for (int i = 1; i < data.length; i++) {
                if (!data[i].startsWith("A")) {
                    break;
                }
                tempArtistList.add(data[i]);

                // Update the play count for the artist fou
                if (playCounts.containsKey(data[i])) {
                    playCounts.put(data[i], playCounts.get(data[i]) + Integer.parseInt(data[data.length - 1]));
                } else {
                    playCounts.put(data[i], Integer.parseInt(data[data.length - 1]));
                }
            }
            artists.put(data[0], tempArtistList);

            // Add info to cache:
            int timesPlayed = Integer.parseInt(data[data.length - 1]);
            musicCounts.put(data[0], (musicCounts.containsKey(data[0]) ? musicCounts.get(data[0]) + timesPlayed : timesPlayed));
        }

        String[] topThreeArtists = new String[3];
        for (int i = 0; i < Math.min(3, playCounts.size()); i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            playCounts.remove(topEntry.getKey());
            topThreeArtists[i] = topEntry.getKey();
        }

        result = topThreeArtists;
        // Create cache entry.
        generateCacheEntry(musicCounts, artists, server);
    }

    /**
     *
     * @param music
     * @param artists
     * @param server
     */
    private void generateCacheEntry(HashMap<String, Integer> music, HashMap<String, ArrayList<String>> artists, Server server){
        // Find the top three played musics
        String[] topThreeMusic = new String[3];
        Integer[] topThreePlays = new Integer[3];
        for (int i = 0; i < Math.min(3, music.size()); i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : music.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            music.remove(topEntry.getKey());
            topThreeMusic[i] = topEntry.getKey();
            topThreePlays[i] = topEntry.getValue();
        }

        // Create value for cache entry
        HashMap<MusicProfile, Integer> musicEntry = new HashMap<>();
        // Add the (up to) 3 songs with their artists.

        for (int i = 0; i < topThreeMusic.length; i++) {
            if (topThreeMusic[i] != null) {
                MusicProfile tempMusicProfile = new MusicProfile(topThreeMusic[i], artists.get(topThreeMusic[i]));
                musicEntry.put(tempMusicProfile, topThreePlays[i]);
            }
        }

        // Make temporary user profile
        UserProfile tempUserProfile = new UserProfile(userID);
        tempUserProfile.favoriteMusics.put(genre, musicEntry);


        // Return cache entry;
        System.err.println("USERPROFILE GENERATED BY GETTOPARTISTS" + tempUserProfile);
        server.addToCache(tempUserProfile);

        this.cache = tempUserProfile;
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
