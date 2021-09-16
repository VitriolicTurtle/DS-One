package Shared;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * Class that gives the number of times a musicID has been played in total based on the dataset.csv.
 */
public class GetTimesPlayedQuery extends Query {
    public String musicID;


    public GetTimesPlayedQuery(int zone, int clientNumber, String musicID) {
        super(zone, clientNumber);
        this.musicID = musicID;
        this.cacheKey = "getTimesPlayed(" + this.musicID + ")";

    }

    public GetTimesPlayedResponse run(String filename) {
        System.out.println("getTimesPlayed from server_" + this.zone);
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
            if(line.contains(this.musicID)){                              // If the line contains the musicID.
                counter+=Integer.parseInt(data[userIndex+1]);             // Add the time listened to the song.
            }
        }
        //System.out.println(this.musicID + " - PLAYED: " + counter);
        return new GetTimesPlayedResponse(zone, clientNumber, counter);
    }

    public GetTimesPlayedResponse cachedRun(List<MusicProfile> cachedMusic, List<UserProfile> cachedUsers){
        //return new GetTimesPlayedResponse(zone, clientNumber, Integer.parseInt(result));
        return null;
    }
    

    @Override
    public String toString() {
        return "GetTimesPlayedQuery(" + musicID + ") zone: " + zone;
    }
}
