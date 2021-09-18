package Shared;

import Server.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class that gives the number of times a userID has played a specific musicID based on the dataset.csv.
 */
public class GetTimesPlayedByUserQuery extends Query {
    // Query arguments
    public String musicID;
    public String userID;

    // Query results
    public int result;

    /**
     * GetTimesPlayedByUser query constructor. The client zone and number of the client sending the query,
     * as well as the arguments for the query, are all determined upon creating the query object.
     *
     * @param clientZone: the zone of the client sending the query.
     * @param clientNumber: the (address) number of the client sending the query.
     * @param musicID: the musicID argument for the query.
     * @param userID: the userID argument for the query.
     */
    public GetTimesPlayedByUserQuery(int clientZone, int clientNumber, String musicID, String userID) {
        super(clientZone, clientNumber);
        this.musicID = musicID;
        this.userID = userID;
    }

    @Override
    public void run(String filename, Server server) {
        Scanner scanner = null;
        int counter = 0;

        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        String tempGenre = "";
        ArrayList<String> tempArtists = new ArrayList<>();
        int tempTimesPlayed = 0;

        //  Scan trough entire dataset and count amount of times listened to song by userID.
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(musicID) || !line.contains(userID)) { continue; }

            String[] data = line.split(",");
            counter += Integer.parseInt(data[data.length - 1]);

            // Get additional info needed for cache entry.
            tempGenre = data[data.length - 3];
            tempArtists.addAll(Arrays.asList(data).subList(1, data.length - 3));
        }
        result = counter;
        // Create cache entry.
        generateCacheEntry(tempGenre, tempArtists, counter, server);
    }

    /**
     *
     * @param genre: Genre used to categorize the music entry in favouriteMusics.
     * @param artists: List of artists to be added to the MusicProfile entry in cache.
     * @param timesPlayed: Times the user has played the song.
     * @param server: Instance of server.
     */
    private void generateCacheEntry(String genre, ArrayList<String> artists, int timesPlayed, Server server){
        HashMap<MusicProfile, Integer> musicEntry = new HashMap<>();
        MusicProfile tempMusicProfile = new MusicProfile(musicID,artists);
        musicEntry.put(tempMusicProfile, timesPlayed);

        // Make temporary user profile
        UserProfile tempUserProfile = new UserProfile(userID);
        tempUserProfile.favoriteMusics.put(genre, musicEntry);

        // Return cache entry;
        server.addToCache(tempUserProfile);
        this.cache = tempUserProfile;
    }

    @Override
    public String toString() {
        String s = "Music '" + musicID + "' was played " + result + " times by user '" + userID + "'. ";
        s += "(Turnaround time: " + (timeStamps[4] - timeStamps[0]) + "ms, ";
        s += "execution time: " + (timeStamps[3] - timeStamps[2]) + "ms, ";
        s += "waiting time: " + (timeStamps[2] - timeStamps[1]) + "ms, ";
        s += "processed by server: " + processingServer + ")";
        return s;
    }
}
