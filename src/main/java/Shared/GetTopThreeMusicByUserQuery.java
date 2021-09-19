package Shared;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

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
    }

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
        HashMap<String, String> musicGenres = new HashMap<>();
        HashMap<String, ArrayList<String>> artists = new HashMap<>();

        while (scanner.hasNextLine()) {
            ArrayList<String> tempArtistList = new ArrayList<>();

            String line = scanner.nextLine();
            if (!line.contains(userID)) {
                continue;
            }

            String[] data = line.split(",");
            String music = data[0];

            for (int i = 1; i < data.length; i++) {
                if (!data[i].startsWith("A")) {
                    break;
                }
                tempArtistList.add(data[i]);
            }
            artists.put(music, tempArtistList);
            musicGenres.put(music, data[data.length-3]);

            int timesPlayed = Integer.parseInt(data[data.length - 1]);
            playCounts.put(music, (playCounts.containsKey(music) ? playCounts.get(music) + timesPlayed : timesPlayed));
        }

        String[] topThreeMusic = new String[3];
        for (int i = 0; i < 3; i++)
            topThreeMusic[i] = "-";

        HashMap<String, Integer> topThreePlayedMusic = new HashMap<>();
        for (int i = 0; i < Math.min(3, playCounts.size()); i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            playCounts.remove(topEntry.getKey());
            topThreeMusic[i] = topEntry.getKey();
            topThreePlayedMusic.put(topEntry.getKey(), topEntry.getValue());
        }

        result = topThreeMusic;
        generateCacheEntry(topThreePlayedMusic, musicGenres, artists, server);
    }

    /**
     *
     * @param music
     * @param genres
     * @param artists
     * @param server
     */
    private void generateCacheEntry(HashMap<String, Integer> music, HashMap<String, String> genres, HashMap<String, ArrayList<String>> artists, Server server) {

        // Sort the music
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

        // -----
        HashMap<String, HashMap<MusicProfile, Integer>> genreMap = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            if (topThreeMusic[i] == null) { break; }
            String musicID = topThreeMusic[i];
            int plays = topThreePlays[i];
            String genre = genres.get(musicID);
            ArrayList<String> artistList = artists.get(musicID);

            MusicProfile musicProfile = new MusicProfile(musicID, artistList);

            if (genreMap.containsKey(genre)) {
                genreMap.get(genre).put(musicProfile, plays);
            } else {
                HashMap<MusicProfile, Integer> musicMap = new HashMap<>();
                musicMap.put(musicProfile, plays);
                genreMap.put(genre, musicMap);
            }
        }
        UserProfile userProfile = new UserProfile(userID);
        userProfile.favoriteMusics = genreMap;
        // -----
        /*
        // Create value for cache entry
        HashMap<MusicProfile, Integer> musicEntry = new HashMap<>();
        // Add the (up to) 3 songs with their artists.
        for (int i = 0; i < topThreeMusic.length; i++) {
            MusicProfile tempMusicProfile = new MusicProfile(topThreeMusic[i], w.get(topThreeMusic[i]));
            musicEntry.put(tempMusicProfile, topThreePlays[i]);
        }

        // Make temporary user profile
        UserProfile tempUserProfile = new UserProfile(userID);


        for (Map.Entry<MusicProfile, Integer> entry : musicEntry.entrySet()) {
            if (tempUserProfile.favoriteMusics.containsKey(genres.get(entry.getKey().musicID))) {
                // Already contains entry for genre
                tempUserProfile.favoriteMusics.get(genres.get(entry.getKey().musicID)).put(entry.getKey(), entry.getValue());
            } else {
                // Does not contain entry for genre
                HashMap<MusicProfile, Integer> tempFavMusic = new HashMap<>();
                tempFavMusic.put(entry.getKey(), entry.getValue());

                tempUserProfile.favoriteMusics.put(genres.get(entry.getKey().musicID), tempFavMusic);
            }
        }
        */
        // Return cache entry;
        System.err.println("USERPROFILE GENERATED IN GETTOPTHREEMUSICBYUSER:\n" + userProfile);
        server.addToCache(userProfile);
        this.cache = userProfile;
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
