package Server.ExecutionServer;

import Shared.*;

import java.util.*;

public class ExecutionServerCache {
    int musicProfileCount = 0;
    int userProfileCount = 0;

    int userProfileCapacity;
    int musicProfileCapacity;

    LinkedHashMap<String, Response> cache = new LinkedHashMap();

    public ExecutionServerCache(int userProfileCapacity, int musicProfileCapacity) {
        this.userProfileCapacity = userProfileCapacity;
        this.musicProfileCapacity = musicProfileCapacity;
    }

    /**
     * Called by the server to cache the response object inside a query.
     *
     * @param query: a query object populated with a response object that should be added to the cache.
     */
    public void update(Query query) {
        Response response = query.response;

        // If the cache already contains the response, we move it to the most recent position in the cache
        if (cache.containsKey(query.getHashString())) {
            cache.remove(query.getHashString());
            cache.put(query.getHashString(), response);
        } else {
            // Update the MusicProfile and UserProfile counts
            if (response.userProfile == null) {
                musicProfileCount++;
            } else {
                userProfileCount++;
                musicProfileCount += countMusicProfiles(response.userProfile);
            }

            // Add the response to the cache
            cache.put(query.getHashString(), response);

            // Ensure that the cache doesn't contain more UserProfile/MusicProfile objects than allowed
            restrain();
        }
    }

    /**
     * Restrain the size of the cache to make sure it never exceeds the UserProfile and MusicProfile capacities.
     */
    private void restrain() {
        // Check whether we need to delete a UserProfile from the cache
        while (userProfileCount > userProfileCapacity) {
            ArrayList<String> keys = new ArrayList<>(cache.keySet());
            Collections.reverse(keys);
            
            // Loop through the keys to the cache (in reversed order)
            for (String key : keys) {
                Response response = cache.get(key);
                
                // Check if the current cache entry contains a UserProfile. If it does, we know that response.musicProfile == null
                if (response.userProfile != null) {
                    // Count how many MusicProfiles are contained within the UserProfile we are removing
                    int numMusicProfiles = countMusicProfiles(response.userProfile);

                    // Update counters
                    userProfileCount--;
                    musicProfileCount -= numMusicProfiles;

                    // Remove the cache entry
                    cache.remove(key);
                    break;
                }
            }
        }

        // Check whether we need to delete a music profile
        while (musicProfileCount > musicProfileCapacity) {
            // Find the last entry of the cache
            Iterator<Map.Entry<String, Response>> it = cache.entrySet().iterator();
            Map.Entry<String, Response> lastEntry = null;

            while (it.hasNext()) {
                lastEntry = it.next();
            }
            assert(lastEntry != null);

            if (lastEntry.getValue().userProfile != null) {
                // Remove a user profile
                userProfileCount--;
                musicProfileCount -= countMusicProfiles(lastEntry.getValue().userProfile);
            } else {
                musicProfileCount--;
            }
            cache.remove(lastEntry.getKey());
        }
    }

    /**
     * Called by the server to try and cache a query.
     *
     * @param query: the query that the cache should try to answer.
     * @return: a response object containing an answer to the query if cache hit, or null otherwise.
     */
    public Response fetch(Query query) {
        String key = query.getHashString();
        if (!cache.containsKey(key))
            return null;

        Response response = cache.get(key);

        // Update the cached entry to be in the most recent position of the cache
        cache.remove(key);
        cache.put(key, response);

        //System.err.println("Cache hit for: " + query.getHashString() + ". Cache size=" + cache.size());
        return response;
    }

    /**
     * Count how many MusicProfile objects are stored inside a given UserProfile object.
     *
     * @param userProfile:
     * @return: how many MusicProfile objects are stored inside userProfile.
     */
    private int countMusicProfiles(UserProfile userProfile) {
        int count = 0;
        for (Map.Entry<String, ArrayList<MusicProfile>> genreEntry : userProfile.favoriteMusics.entrySet()) {
            for (MusicProfile musicEntry : genreEntry.getValue()) {
                count++;
            }
        }
        return count;
    }
}
