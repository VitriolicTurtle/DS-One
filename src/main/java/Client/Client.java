package Client;

import Server.ProxyServerInterface;
import Server.ServerInterface;
import Shared.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements ClientCallbackInterface, Serializable {
    private int clientNumber;
    private Registry registry = null;

    private boolean clientCache;
    private LinkedList<UserProfile> userCache = new LinkedList<>();
    private LinkedHashMap<MusicProfile, Integer> musicCache = new LinkedHashMap<>(100);

    private LinkedList<Query> responses = new LinkedList<>();
    private int sentQueries = 0;
    private int finishedCount = -1;


    private ProxyServerInterface proxyServer = null;
    private ServerInterface server = null;

    // Used to make sure only one server can send back a response at a time
    Lock lock = new ReentrantLock();

    // Variables to store average times for the different query-types
    long getTimesPlayedByUserTurnaround = 0;
    long getTimesPlayedByUserExecution = 0;
    long getTimesPlayedByUserWaiting = 0;
    long getTimesPlayedTurnaround = 0;
    long getTimesPlayedExecution = 0;
    long getTimesPlayedWaiting = 0;
    long getTopArtistsByUserGenreTurnaround = 0;
    long getTopArtistsByUserGenreExecution = 0;
    long getTopArtistsByUserGenreWaiting = 0;
    long getTopThreeMusicByUserTurnaround = 0;
    long getTopThreeMusicByUserExecution = 0;
    long getTopThreeMusicByUserWaiting = 0;

    /**
     * Constructor for client.
     *
     * @param clientNumber: unique ID for the client.
     */
    public Client(int clientNumber, int port, boolean clientCache) {
        this.clientNumber = clientNumber;
        startClient(port);
    }

    /**
     * Finds and uses the registry to lookup the proxy-server.
     */
    private void startClient(int port) {
        try {
            // Get the registry
            registry = LocateRegistry.getRegistry("localhost", port - 7);

            // Lookup the proxy-server
            proxyServer = (ProxyServerInterface) registry.lookup("proxy-server");

            // Export the client to the registry
            UnicastRemoteObject.exportObject(this, port);

            // Bind the client to the registry
            registry.bind("client_" + clientNumber, this);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to start client_" + clientNumber + ".");
            System.exit(1);
        }
        System.out.println("client_" + clientNumber + " has started successfully.");
    }

    public void finished(int count) {
        finishedCount = count;
    }

    /**
     * Remote method invoked by the server to respond to a query already sent out by the client.
     *
     * @param response: the query object populated with a response.
     * @throws RemoteException
     */
    public void sendQueryResponse(Query response) throws RemoteException {
        lock.lock();

        System.out.println("Client received query response.");
        System.out.println("Received responses: " + responses.size());
        acceptResponse(response);

        lock.unlock();
    }

    private void acceptResponse(Query response) {
        responses.add(response);

        // Set the final event timestamp representing that the query has been returned to the client object
        response.timeStamps[4] = System.currentTimeMillis();

        if (response.containsCacheData) {
            System.out.println("Caching response data in client.");

            if (response instanceof GetTimesPlayedByUserQuery) {
                cacheGetTimesPlayedByUser(
                        ((GetTimesPlayedByUserQuery) response).userID,
                        ((GetTimesPlayedByUserQuery) response).musicID,
                        ((GetTimesPlayedByUserQuery) response).genre,
                        ((GetTimesPlayedByUserQuery) response).artists,
                        ((GetTimesPlayedByUserQuery) response).result);
            } else if (response instanceof GetTimesPlayedQuery) {
                cacheGetTimesPlayed(
                        ((GetTimesPlayedQuery) response).musicID,
                        ((GetTimesPlayedQuery) response).artists,
                        ((GetTimesPlayedQuery) response).result);
            } else if (response instanceof GetTopArtistsByUserGenreQuery) {
                cacheGetTopArtistsByUserGenre(
                        ((GetTopArtistsByUserGenreQuery) response).userID,
                        ((GetTopArtistsByUserGenreQuery) response).genre,
                        ((GetTopArtistsByUserGenreQuery) response).topThreeProfiles,
                        ((GetTopArtistsByUserGenreQuery) response).topThreePlayCounts);
            } else if (response instanceof GetTopThreeMusicByUserQuery) {
                cacheGetTopThreeMusicByUser(
                        ((GetTopThreeMusicByUserQuery) response).userID,
                        ((GetTopThreeMusicByUserQuery) response).topThreeProfiles,
                        ((GetTopThreeMusicByUserQuery) response).topThreePlayCounts,
                        ((GetTopThreeMusicByUserQuery) response).topThreeGenres);
            }
        }

        if (finishedCount != -1 && responses.size() == finishedCount) {
            conclude();
        }
    }

    /**
     * Get a server assignment from the proxy-server, parse the query and build a query object,
     * then send the query object to the server assigned by the proxy-server.
     *
     * @param queryString: the query as a string.
     * @param zone:        the zone in which the client is sending the query from.
     */
    public void processQuery(String queryString, int zone) {
        // Get a server assignment from the proxy-server
        getServerAssignment(zone);

        // Parse the query
        String[] data = queryString.split("\\(");
        String method = data[0];
        String[] arguments = data[1].substring(0, data[1].length() - 1).split(",");

        // Build the query object and send the query object to the server for processing
        try {
            Query query = null;
            switch (method) {
                case "getTimesPlayed" -> {
                    assert (arguments.length == 1);
                    query = new GetTimesPlayedQuery(zone, clientNumber, arguments[0]);
                }
                case "getTimesPlayedByUser" -> {
                    assert (arguments.length == 2);
                    query = new GetTimesPlayedByUserQuery(zone, clientNumber, arguments[0], arguments[1]);
                }
                case "getTopThreeMusicByUser" -> {
                    assert (arguments.length == 1);
                    query = new GetTopThreeMusicByUserQuery(zone, clientNumber, arguments[0]);
                }
                case "getTopArtistsByUserGenre" -> {
                    assert (arguments.length == 2);
                    query = new GetTopArtistsByUserGenreQuery(zone, clientNumber, arguments[0], arguments[1]);
                }
                default -> {
                    System.out.println("\nError:\nInvalid remote method query: '" + method + "'.");
                    System.exit(1);
                }
            }
            boolean cacheHit = false;
            if (clientCache) {
                if (query instanceof GetTimesPlayedByUserQuery) {
                    cacheHit = cache((GetTimesPlayedByUserQuery) query);
                } else if (query instanceof GetTimesPlayedQuery) {
                    cacheHit = cache((GetTimesPlayedQuery) query);
                } else if (query instanceof GetTopArtistsByUserGenreQuery) {
                    cacheHit = cache((GetTopArtistsByUserGenreQuery) query);
                } else if (query instanceof GetTopThreeMusicByUserQuery) {
                    cacheHit = cache((GetTopThreeMusicByUserQuery) query);
                }
            }

            if (cacheHit) {
                for (int i = 0; i < 5; i++)
                    query.timeStamps[i] = System.currentTimeMillis();

                acceptResponse(query);
            } else {
                // Finally, set the timestamp for when the query is sent from the client, then send it to the server
                query.timeStamps[0] = System.currentTimeMillis();
                server.sendQuery(query);
            }
            sentQueries++;

            System.out.println("Client sent query. Number of sent queries: " + sentQueries);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to send query from client_" + clientNumber + " to " + server + ".");
            System.exit(1);
        }
    }

    private void conclude() {
        System.out.println("Writing query responses to file ...");
        try {
            //File file = new File("src\\main\\java\\Client\\Outputs\\output.txt"); // WINDOWS
            File file = new File("src/main/java/Client/Outputs/output.txt"); // MAC
            FileWriter writer = new FileWriter(file);

            while (responses.size() != 0) {
                Query response = responses.remove();
                writer.write(response.toString() + "\n");

                // Add the query's turnaround, execution and waiting time to the average statistics
                long turnaround = response.timeStamps[4] - response.timeStamps[0];
                long execution = response.timeStamps[3] - response.timeStamps[2];
                long waiting = response.timeStamps[2] - response.timeStamps[1];

                if (response instanceof GetTimesPlayedByUserQuery) {
                    getTimesPlayedByUserTurnaround += turnaround;
                    getTimesPlayedByUserExecution += execution;
                    getTimesPlayedByUserWaiting += waiting;
                } else if (response instanceof GetTimesPlayedQuery) {
                    getTimesPlayedTurnaround += turnaround;
                    getTimesPlayedExecution += execution;
                    getTimesPlayedWaiting += waiting;
                } else if (response instanceof GetTopArtistsByUserGenreQuery) {
                    getTopArtistsByUserGenreTurnaround += turnaround;
                    getTopArtistsByUserGenreExecution += execution;
                    getTopArtistsByUserGenreWaiting += waiting;
                } else if (response instanceof GetTopThreeMusicByUserQuery) {
                    getTopThreeMusicByUserTurnaround += turnaround;
                    getTopThreeMusicByUserExecution += execution;
                    getTopThreeMusicByUserWaiting += waiting;
                }
            }

            // Write the average times to file
            writer.write("\nAverage turnaround time for getTimesPlayedByUser queries: " + getTimesPlayedByUserTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTimesPlayedByUser queries: " + getTimesPlayedByUserExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTimesPlayedByUser queries: " + getTimesPlayedByUserWaiting / sentQueries + "ms\n\n");

            writer.write("Average turnaround time for getTimesPlayed queries: " + getTimesPlayedTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTimesPlayed queries: " + getTimesPlayedExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTimesPlayed queries: " + getTimesPlayedWaiting / sentQueries + "ms\n\n");

            writer.write("Average turnaround time for getTopArtistsByUserGenre queries: " + getTopArtistsByUserGenreTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTopArtistsByUserGenre queries: " + getTopArtistsByUserGenreExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTopArtistsByUserGenre queries: " + getTopArtistsByUserGenreWaiting / sentQueries + "ms\n\n");

            writer.write("Average turnaround time for getTopThreeMusicByUser queries: " + getTopThreeMusicByUserTurnaround / sentQueries + "ms\n");
            writer.write("Average execution time for getTopThreeMusicByUser queries: " + getTopThreeMusicByUserExecution / sentQueries + "ms\n");
            writer.write("Average waiting time for getTopThreeMusicByUser queries: " + getTopThreeMusicByUserWaiting / sentQueries + "ms\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("All query responses have been written to file.");
    }

    /**
     * Prompts the proxy-server to assign the client a server, then lookups the server address returned
     * from the proxy-server.
     *
     * @param zone: the zone in which the client is in.
     */
    private void getServerAssignment(int zone) {
        try {
            // Ask the proxy-server for a server address
            ServerAddress response = proxyServer.getServerAssignment(zone);

            // Lookup the returned server address
            server = (ServerInterface) registry.lookup(response.address);
        } catch (Exception e) {
            System.out.println("\nError:\n" + e);
            System.out.println("\nSomething went wrong when trying to get server assignment in client_" + clientNumber + ".");
            System.exit(1);
        }
    }

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
        for (int i = 0; i < 3; i++) {
            Map.Entry<MusicProfile, Integer> topEntry = null;
            for (Map.Entry<MusicProfile, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            if (topEntry == null)
                break;

            playCounts.remove(topEntry.getKey());

            query.result[i] = topEntry.getKey().musicID;
        }

        System.err.println("Cache hit for: GetTopThreeMusicByUser");
        // Return true (a cache hit)
        return true;
    }

    /**
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
        for (int i = 0; i < 3; i++) {
            Map.Entry<MusicProfile, Integer> topEntry = null;
            for (Map.Entry<MusicProfile, Integer> entry : musicMap.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ? entry : topEntry;
            }
            if (topEntry == null)
                break;

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
        if (userCache.size() >= 100)
            userCache.remove();

        userCache.add(userProfile);
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
}