package Shared;

import java.io.File;
import java.util.Scanner;

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

    @Override
    public String toString() {
        return "GetTimesPlayedByUserQuery(" + musicID + ", " + userID + ") zone: " + zone;
    }
}
