package Shared;

import javax.swing.*;
import java.io.File;
import java.util.*;

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
        this.cacheKey = "getTimesPlayedByUser(" + this.musicID + ", " + this.userID + ")";
    }

    @Override
    public void run(String filename) {
        Scanner scanner = null;
        int counter = 0;

        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        //  Scan trough entire dataset and count amount of times listened to song by userID.
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(musicID) || !line.contains(userID)) { continue; }

            String[] data = line.split(",");
            counter += Integer.parseInt(data[data.length - 1]);
        }


        result = counter;
    }

    public boolean serverCacheRun(List<UserProfile> cachedUsers){
        // Find user profile object in list
        UserProfile tempUser = cachedUsers.stream().filter(user -> this.userID.equals(user.UserID)).findFirst().orElse(null);
        if(tempUser != null){
            // Default value -1
            int cachedResult = -1;
            // Scan the entire favouriteMusics hashmap to see if it exists there.
            // For each favouriteMusics object (genre).
            for(Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : tempUser.favoriteMusics.entrySet()){
                // For each favouriteMusics object value (songs in hashmap by genre key).
                for(Map.Entry<MusicProfile, Integer> songEntry: genreEntry.getValue().entrySet()){
                    // Check if the musicID is present.
                    if(songEntry.getKey().musicID.equals(this.musicID)){
                        cachedResult = songEntry.getValue();
                    }
                }
            }
            System.err.println("CACHED " + cachedResult);
            if(cachedResult >= 0) {
                result = cachedResult;
                return true;
            } else{
                return false;
            }

            //return new GetTimesPlayedByUserResponse(zone, clientNumber, Integer.parseInt(result));

        }
        return false;
    }
    /*
    public GetTimesPlayedByUserResponse cachedRun(List<MusicProfile> cachedMusic, List<UserProfile> cachedUsers){
        UserProfile tempUser = cachedUsers.stream().filter(user -> this.userID.equals(user.UserID)).findFirst().orElse(null);
        if(tempUser != null){
            Object[] favoriteMusicsArray = tempUser.favoriteMusics.entrySet().toArray();              // Array object created to sort HashMap by value.
            Arrays.sort(favoriteMusicsArray, new Comparator(){                                        // Sorts the array based on custom comparator function.
                public int compare(Object val1, Object val2){
                    return((Map.Entry<String, MusicProfile>) val2).getValue().musicID.compareTo(((Map.Entry<String, MusicProfile>) val1).getValue().musicID);
                }
            });

            String result = "";
            result = ((Map.Entry<String, MusicProfile>) favoriteMusicsArray[0]).getValue().musicID;
            System.err.println("CACHED " + result);
            result = "1";

            return new GetTimesPlayedByUserResponse(zone, clientNumber, Integer.parseInt(result));

        }


        //return new GetTimesPlayedByUserResponse(zone, clientNumber, Integer.parseInt(result));
        return null;
    }
    */

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
