package Shared;

import java.io.File;
import java.util.*;

/**
 * Class that gives the number of times a userID has played a specific musicID based on the dataset.csv.
 */
public class GetTimesPlayedByUserQuery extends Query {
    public String musicID;
    public String userID;

    public GetTimesPlayedByUserQuery(int zone, int clientNumber, String musicID, String userID) {
        super(zone, clientNumber);
        this.musicID = musicID;
        this.userID = userID;
        this.cacheKey = "getTimesPlayedByUser(" + this.musicID + ", " + this.userID + ")";
    }

    public GetTimesPlayedByUserResponse run(String filename) {
        System.out.println("getTimesPlayedByUser from server_" + this.zone);
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
            int userIndex = 3;                                            // Smallest index for user is 3 because there is always minimum 1 artist
            String line = scanner.nextLine();
            String[] data = line.split(",");
            while (!data[userIndex].startsWith("U")){                     // If there are more artists than 1, loop through indexes to find user.
                userIndex++;
            }
            if(data[0].equals(this.musicID) && data[userIndex].equals(this.userID)) {
                counter+=Integer.parseInt(data[userIndex+1]);
            }
        }
        return new GetTimesPlayedByUserResponse(zone, clientNumber, counter);
    }

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

    @Override
    public String toString() {
        return "GetTimesPlayedByUserQuery(" + musicID + ", " + userID + ") zone: " + zone;
    }
}
