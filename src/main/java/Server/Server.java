package Server;

import Client.ClientCallbackInterface;
import Shared.*;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements ServerInterface {
    private Registry registry = null;

    private int serverZone;
    private int port;

    private Boolean serverCaching;

    private LinkedList<UserProfile> userCache = new LinkedList<>();
    private LinkedHashMap<MusicProfile, Integer> musicCache = new LinkedHashMap<>(100);

    ConcurrentLinkedQueue<Query> queue = new ConcurrentLinkedQueue<>();

    //private final String dataFilename = "src\\main\\java\\Server\\Data\\dataset.csv"; // Windows
    private final String dataFilename = "src/main/java/Server/Data/dataset.csv"; // MAC

    /**
     * Constructor for server.
     *
     * @param serverZone: which geographical zone the server is in.
     * @param port:       the port the server is running on.
     */
    public Server(Registry registry, int serverZone, int port, Boolean serverCaching) {
        this.registry = registry;
        this.serverZone = serverZone;
        this.port = port;
        this.serverCaching = serverCaching;
        startServer();
        startProcessingThread();
    }

    /**
     * Exports the server object to the registry.
     */
    private void startServer() {
        try {
            // Export the server to the registry
            UnicastRemoteObject.exportObject(this, port);

            // Bind the server to the registry
            registry.bind("server_" + serverZone, this);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start server_" + serverZone + ".");
            System.exit(1);
        }
        System.out.println("server_" + serverZone + " has started successfully.");
    }

    /**
     *
     * @param query
     * @return
     */
    public boolean cache(GetTimesPlayedByUserQuery query) {
        UserProfile userProfile = null;

        // Check if the cache contains a UserProfile object for the user
        for (UserProfile user : userCache) {
            if (query.userID.equals(user.userID)) {
                userProfile = user;
            }
        }

        // If no user object was found for the user, false (a cache miss) is returned
        if (userProfile == null)
            return false;

        // Search the user profile object to see if an entry for the music exists

        // Loop through each genre's value map in the user profile
        for (Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : userProfile.favoriteMusics.entrySet()) {

            // Loop through each music profile entry in the genre's map value
            for (Map.Entry<MusicProfile, Integer> musicEntry : genreEntry.getValue().entrySet()) {

                // Check if we have found the music we are looking for
                if (query.musicID.equals(musicEntry.getKey().musicID)) {

                    // If we have found the correct music, we add the cached play count to the query result and
                    // return true (a cache hit)
                    query.result = musicEntry.getValue();

                    // Remove the genre entry from the favoriteMusics map, then re-add it to place it as the most
                    // recent genre queried in the cache
                    userProfile.favoriteMusics.remove(genreEntry.getKey());
                    userProfile.favoriteMusics.put(genreEntry.getKey(), genreEntry.getValue());

                    System.err.println("Cache hit for: GetTimesPlayedByUser");
                    return true;
                }
            }
        }

        // If no entry for the music were found in the user profile, we return false (a cache miss)
        return false;
    }

    /**
     *
     * @param query
     * @return
     */
    public boolean cache(GetTimesPlayedQuery query) {
        for (Map.Entry<MusicProfile, Integer> musicEntry : musicCache.entrySet()) {
            // If we have a cache hit
            if (musicEntry.getKey().musicID.equals(query.musicID)) {
                query.result = musicEntry.getValue();
                System.err.println("Cache hit for: GetTimesPlayed");
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param query
     * @return
     */
    public boolean cache(GetTopThreeMusicByUserQuery query) {
        UserProfile userProfile = null;

        // Search the cache for a user profile for the current user
        for (UserProfile user : userCache) {
            if (query.userID.equals(user.userID)) {
                userProfile = user;
            }
        }

        // If no user profile was found in the cache, we return false (a cache miss)
        if (userProfile == null)
            return false;

        // Collect the play counts of all the songs found in the cache for this user profile
        HashMap<MusicProfile, Integer> playCounts = new HashMap<>();
        // For each genre-hashmap entry in the userProfile.favoriteMusics map
        for (Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : userProfile.favoriteMusics.entrySet()) {
            for (Map.Entry<MusicProfile, Integer> musicEntry : genreEntry.getValue().entrySet()) {
                playCounts.put(musicEntry.getKey(), musicEntry.getValue());
            }
        }

        // Find the top 3 most played music entries
        for (int i = 0; i < Math.min(3, playCounts.size()); i++) {
            Map.Entry<MusicProfile, Integer> topEntry = null;
            for (Map.Entry<MusicProfile, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            playCounts.remove(topEntry.getKey());

            query.result[i] = topEntry.getKey().musicID;
        }

        System.err.println("Cache hit for: GetTopThreeMusicByUser");
        // Return true (a cache hit)
        return true;
    }

    /**
     *
     * @param query
     * @return
     */
    public boolean cache(GetTopArtistsByUserGenreQuery query) {
        UserProfile userProfile = null;

        // Search the cache for a user profile for the current user
        for (UserProfile user : userCache) {
            if (query.userID.equals(user.userID)) {
                userProfile = user;
            }
        }

        // If no user profile was found in the cache, or
        // if the userProfile found does not have an entry for the queried genre, we return false (a cache miss)
        if (userProfile == null || !userProfile.favoriteMusics.containsKey(query.genre))
            return false;

        // Fetch the musicMap containing the music profiles and their respective play counts from the user profile
        // indexed by the genre
        HashMap<MusicProfile, Integer> musicMap = new HashMap<>(userProfile.favoriteMusics.get(query.genre));
        int resultIdx = 0;

        // Find the most played music profiles in the cache for this genre and user, in order from most played to least
        for (int i = 0; i < Math.min(3, musicMap.size()); i++) {
            Map.Entry<MusicProfile, Integer> topEntry = null;
            for (Map.Entry<MusicProfile, Integer> entry : musicMap.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            musicMap.remove(topEntry.getKey());

            // Add the artists to the query result
            for (int j = 0; j < topEntry.getKey().artists.size(); j++) {
                if (resultIdx >= 3 || topEntry.getKey().artists.get(j) == null)
                    break;

                query.result[resultIdx] = topEntry.getKey().artists.get(j);
                resultIdx++;
            }
        }

        System.err.println("Cache hit for: GetTopArtistsByUserGenre");
        // Return true (a cache hit)
        return true;
    }

    public void cacheGetTimesPlayedByUser(String userID, String musicID, String genre, ArrayList<String> artists, int plays) {
        MusicProfile musicProfile = new MusicProfile(musicID, artists);
        UserProfile userProfile = null;

        // Check whether the user has a user profile in the cache already
        for (UserProfile user : userCache) {
            if (user.userID.equals(userID)) {
                userProfile = user;

                // We also remove the user profile found from the cache since it will be re-added into the
                // most recent position
                userCache.remove(userProfile);
                break;
            }
        }

        // If the user profile did not already exist in the cache, we create a new one
        if (userProfile == null)
            userProfile = new UserProfile(userID);

        // Add the new musicProfile to the userProfile

        // If the favoriteMusics map doesn't contain an entry for this genre
        if (!userProfile.favoriteMusics.containsKey(genre)) {

            // If the favoriteMusics map already contains 3 or more genre entries we remove the oldest entry
            //if (userProfile.favoriteMusics.size() >= 3)
            //    userProfile.favoriteMusics.remove(userProfile.favoriteMusics.entrySet().iterator().next().getKey());

            // Add the new genre to the favoriteMusics map
            userProfile.favoriteMusics.put(genre, new HashMap<MusicProfile, Integer>());
        }

        // Add the music profile and its play count to the mapping value for the music's genre
        userProfile.favoriteMusics.get(genre).put(musicProfile, plays);

        // If the userCache is at max capacity, we remove the oldest entry
        if (userCache.size() >= 100)
            userCache.remove();

        userCache.add(userProfile);
    }

    public void cacheGetTimesPlayed(String musicID, ArrayList<String> artists, int plays) {
        MusicProfile musicProfile = new MusicProfile(musicID, artists);

        if (musicCache.containsKey(musicProfile)) {
            // If the music profile is cached already, we remove it and re-add the new one to move it to the front
            // (most recent)
            musicCache.remove(musicProfile);
        } else if (musicCache.size() >= 100) {
            // If the cache is at max capacity we remove the oldest entry
            musicCache.remove(musicCache.entrySet().iterator().next().getKey());
        }

        // Add the new entry to the cache
        musicCache.put(musicProfile, plays);
    }

    public void cacheGetTopThreeMusicByUser(
            String userID, MusicProfile[] topThreeMusicProfiles, int[] topThreePlayCounts, String[] topThreeGenres) {
        UserProfile userProfile = null;

        // Check whether the user has a user profile in the cache already
        for (UserProfile user : getTopThreeMusicByUserCache) {
            if (user.userID.equals(userID)) {
                userProfile = user;

                // We also remove the user profile found from the cache since it will be re-added into the
                // most recent position
                getTopThreeMusicByUserCache.remove(userProfile);
                break;
            }
        }

        // If the user profile did not already exist in the cache, we create a new one
        if (userProfile == null)
            userProfile = new UserProfile(userID);

        // Add the provided music profiles to the cache user profile
        for (int i = 0; i < 3; i++) {
            MusicProfile musicProfile = topThreeMusicProfiles[i];
            int plays = topThreePlayCounts[i];
            String genre = topThreeGenres[i];

            // Since a user could have listened to less than 3 artists and/or songs throughout the entire dataset,
            // we could potentially have less than 3 valid music profiles here
            if (musicProfile == null)
                break;

            // If the user profile doesn't already have an entry for the provided genre, we need to add one
            if (!userProfile.favoriteMusics.containsKey(genre)) {
                // If the favoriteMusics map is at max capacity, we remove the oldest entry
                //if (userProfile.favoriteMusics.size() >= 3)
                //    userProfile.favoriteMusics.remove(userProfile.favoriteMusics.entrySet().iterator().next().getKey());

                // Create the new entry for the genre in the favoriteMusics map
                userProfile.favoriteMusics.put(genre, new HashMap<MusicProfile, Integer>());
            }

            // Add the music profile and its plays to the entry
            userProfile.favoriteMusics.get(genre).put(musicProfile, plays);
        }

        // If the userCache is at max capacity, we remove the oldest entry
        if (getTopThreeMusicByUserCache.size() >= 100)
            getTopThreeMusicByUserCache.remove();

        getTopThreeMusicByUserCache.add(userProfile);
    }

    public void cacheGetTopArtistsByUserGenre(
            String userID, String genre, MusicProfile[] topThreeProfiles, int[] topThreePlayCounts) {
        UserProfile userProfile = null;

        // Check whether the user has a user profile in the cache already
        for (UserProfile user : userCache) {
            if (user.userID.equals(userID)) {
                userProfile = user;

                // We also remove the user profile found from the cache since it will be re-added into the
                // most recent position
                userCache.remove(userProfile);
                break;
            }
        }

        // If the user profile did not already exist in the cache, we create a new one
        if (userProfile == null)
            userProfile = new UserProfile(userID);

        // Add the provided music profiles to the cache user profile

        // If the user profile favoriteMusics doesn't already have an entry for the current genre
        if (!userProfile.favoriteMusics.containsKey(genre)) {
            // If the favoriteMusics map is at max capacity, we remove the oldest entry
            //if (userProfile.favoriteMusics.size() >= 3)
            //    userProfile.favoriteMusics.remove(userProfile.favoriteMusics.entrySet().iterator().next().getKey());

            // Add an entry for the new genre
            userProfile.favoriteMusics.put(genre, new HashMap<MusicProfile, Integer>());
        }

        // Add the music profiles provided to the genre entry in favoriteMusics
        for (int i = 0; i < 3; i++) {
            // Since a user could have listened to less than 3 artists and/or songs throughout the entire dataset,
            // we could potentially have less than 3 valid music profiles here
            if (topThreeProfiles[i] == null)
                break;

            userProfile.favoriteMusics.get(genre).put(topThreeProfiles[i], topThreePlayCounts[i]);
        }

        // If the user cache is at max capacity, we remove the oldest entry
        if (userCache.size() >= 100)
            userCache.remove();

        userCache.add(userProfile);
    }

    /**
     * Main processing thread. This handles requests as they are added to the queue.
     */
    public void startProcessingThread() {
        new Thread(new ServerQueryProcessor(this, this.dataFilename, serverCaching)).start();
    }

    /**
     * ServerInterface method that allows for the proxy-server to get the server's current query queue size.
     *
     * @return int: the current size of the query queue.
     * @throws RemoteException
     */
    public int getQueueSize() throws RemoteException {
        return queue.size();
    }

    /**
     * ServerInterface method that allows for clients to send queries for processing.
     *
     * @param query: a query object containing information about which client from which zone has sent the query,
     *               as well as what the query and the query's arguments are.
     * @throws RemoteException
     */
    public void sendQuery(Query query) throws RemoteException {
        query.timeStamps[1] = System.currentTimeMillis();
        queue.add(query);
        System.out.println("Query added to server_" + serverZone + " queue. Queue size: " + queue.size());
    }

    /**
     * Respond to a client by sending the query object back. The query object will be populated with the
     * query result.
     * The query object contains all the necessary data to lookup the correct client that originally sent the query,
     * so we know that we return the response to the correct client.
     *
     * @param query: the query (now populated with a query result) being sent to the client.
     */
    public void sendResponse(Query query) {
        try {
            System.out.println("server_" + serverZone + " sending query response to client.");

            // Use the registry to lookup the client that is being responded to
            ClientCallbackInterface client = (ClientCallbackInterface) registry.lookup("client_" + query.getClientNumber());

            // Send the query (that is now populated with a response) back to the client
            client.sendQueryResponse(query);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("Something went wrong when responding to client_" + query.getClientNumber() + " from server_" + serverZone);
            System.exit(1);
        }
    }

    /**
     * Fetches a query object from the query queue if the queue isn't empty.
     *
     * @return: a query object, unless the queue is empty in which case null is returned.
     */
    public Query fetchQuery() {
        if (this.queue.size() > 0) {
            return queue.remove();
        }
        return null;
    }

    /**
     * Get the geographical zone of this server.
     *
     * @return: the server zone.
     */
    public int getServerZone() {
        return serverZone;
    }

    /**
     * Returns the server name as a string.
     *
     * @return: the server name.
     */
    @Override
    public String toString() {
        return "server_" + serverZone;
    }
}