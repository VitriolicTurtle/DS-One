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
    private LinkedList<UserProfile> cache = new LinkedList<>();

    ConcurrentLinkedQueue<Query> queue = new ConcurrentLinkedQueue<>();

    private final String dataFilename = "src\\main\\java\\Server\\Data\\dataset.csv"; // Windows
    //private final String dataFilename = "src/main/java/Server/Data/dataset.csv"; // MAC

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
     */
    public boolean searchCache(GetTimesPlayedByUserQuery query) {
        // Find user profile object in list
        UserProfile tempUser = cache.stream().filter(user -> query.userID.equals(user.userID)).findFirst().orElse(null);
        if(tempUser != null){
            // Default value -1
            int cachedResult = -1;
            // Scan the entire favouriteMusics hashmap to see if it exists there.
            // For each favouriteMusics object (genre).
            for(Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : tempUser.favoriteMusics.entrySet()){
                // For each favouriteMusics object value (songs in hashmap by genre key).
                for(Map.Entry<MusicProfile, Integer> songEntry: genreEntry.getValue().entrySet()){
                    // Check if the musicID is present.
                    if(songEntry.getKey().musicID.equals(query.musicID)){
                        cachedResult = songEntry.getValue();
                    }
                }
            }
            if(cachedResult >= 0) {
                query.result = cachedResult;
                //System.err.println("CACHED " + cachedResult);
                return true;
            }
        }
        return false;

    }

    /**
     *
     */
    public boolean searchCache(GetTimesPlayedQuery query) {
        int cachedResult = 0;
        for(UserProfile user : cache){
            for(Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : user.favoriteMusics.entrySet()){
                // For each favouriteMusics object value (songs in hashmap by genre key).
                for(Map.Entry<MusicProfile, Integer> songEntry: genreEntry.getValue().entrySet()){
                    // Check if the musicID is present.
                    if(songEntry.getKey().musicID.equals(query.musicID)){
                        cachedResult += songEntry.getValue();
                    }
                }
            }
        }

        if(cachedResult > 0) {
            //System.err.println("CACHED " + cachedResult);
            query.result = cachedResult;
            return true;
        }

        return false;
    }

    /**
     *
     */
    public boolean searchCache(GetTopArtistsByUserGenreQuery query) {
        String userID = query.userID;
        String genre = query.genre;
        boolean hit = false;

        // Check if cache miss, and return false without doing any further work if so
        UserProfile profile = null;
        for (UserProfile userProfile : cache) {
            if (Objects.equals(userProfile.userID, userID)) {
                profile = userProfile;

                // Check that the cache can provide an answer to the query. We have a cache miss if it cannot
                hit = (profile.favoriteMusics.containsKey(genre));
                break;
            }
        }
        if (!hit) { return false; }

        // Find the query result from the cache
        HashMap<String, Integer> artistPlayCounts = new HashMap<>();

        for (Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : profile.favoriteMusics.entrySet()) {
            if (genreEntry.getKey() != genre) { continue; }
            for (Map.Entry<MusicProfile, Integer> entry : genreEntry.getValue().entrySet()) {
                int plays = entry.getValue();
                ArrayList<String> artists = entry.getKey().artists;
                for (String artist : artists) {
                    if (artistPlayCounts.containsKey(artist)) {
                        artistPlayCounts.put(artist, artistPlayCounts.get(artist) + plays);
                    } else {
                        artistPlayCounts.put(artist, plays);
                    }
                }
            }
        }

        String[] topArtists = new String[3];
        for (int i = 0; i < 3; i++)
            topArtists[i] = "Q";

        // Find the top 3 artists from the artistPlayCount map
        for (int i = 0; i < Math.min(3, artistPlayCounts.size()); i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : artistPlayCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            artistPlayCounts.remove(topEntry.getKey());
            topArtists[i] = topEntry.getKey();
        }

        query.result = topArtists;
        return true;
    }

    /**
     *
     */
    public boolean searchCache(GetTopThreeMusicByUserQuery query) {
        String userID = query.userID;
        boolean hit = false;

        // Check if cache miss, and return false without doing any further work if so
        UserProfile profile = null;
        for (UserProfile userProfile : cache) {
            if (Objects.equals(userProfile.userID, userID)) {
                profile = userProfile;
                hit = true;
                break;
            }
        }
        if (!hit) { return false; }

        // Find the query result from the cache
        HashMap<String, Integer> musicPlayCounts = new HashMap<>();

        for (Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : profile.favoriteMusics.entrySet()) {
            for (Map.Entry<MusicProfile, Integer> entry : genreEntry.getValue().entrySet()) {
                int plays = entry.getValue();
                String music = entry.getKey().musicID;
                if (musicPlayCounts.containsKey(music)) {
                    musicPlayCounts.put(music, musicPlayCounts.get(music) + plays);
                } else {
                    musicPlayCounts.put(music, plays);
                }
            }
        }

        String[] topMusic = new String[3];
        for (int i = 0; i < 3; i++)
            topMusic[i] = "Q";

        // Find the top 3 musics from the musicPlayCounts map
        for (int i = 0; i < Math.min(3, musicPlayCounts.size()); i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : musicPlayCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            musicPlayCounts.remove(topEntry.getKey());
            topMusic[i] = topEntry.getKey();
        }

        query.result = topMusic;
        return true;
    }

    /**
     *
     */
    public void addToCache(UserProfile userProfile) {
        UserProfile previous = null;
        for (UserProfile cacheProfile : cache) {
            if (cacheProfile.userID.equals(userProfile.userID)) {
                previous = cacheProfile;
                break;
            }
        }

        // If we have no previous entry for this user in the cache
        if (previous == null) {
            if (cache.size() >= 100) { cache.remove(); }
            cache.add(userProfile);
            return;
        }

        // Otherwise we have found a previous userprofile entry in the cache for this user, so we merge their contents and
        // move it to the back of the list (most recent)

        // Remove the previous entry
        cache.remove(previous);
        
        // Merge the previous and new entry
        for (Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : previous.favoriteMusics.entrySet()) {
            // If data about the genre exists in both the previous and new entry, we merge the contents
            if (userProfile.favoriteMusics.containsKey(genreEntry.getKey())) {
                for (Map.Entry<MusicProfile, Integer> musicEntry : genreEntry.getValue().entrySet()) {
                    if (userProfile.favoriteMusics.get(genreEntry.getKey()).containsKey(musicEntry.getKey())) {
                        continue;
                    } else {
                        userProfile.favoriteMusics.get(genreEntry.getKey()).put(musicEntry.getKey(), musicEntry.getValue());
                    }
                }
            }
            // Otherwise we just add the extra genre data to the new entry so that it contains all the new and old data
            else {
                userProfile.favoriteMusics.put(genreEntry.getKey(), genreEntry.getValue());
            }
        }

        // Finally, add the merged userprofile object to the end of the list (most recent)
        cache.add(userProfile);
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
