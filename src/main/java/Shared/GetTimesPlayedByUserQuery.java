package Shared;

import java.io.File;
import java.util.Scanner;

/**
 * Class that gives the number of times a userID has played a specific musicID based on the dataset.csv.
 */
public class GetTimesPlayedByUserQuery extends Query {
    public String musicID;
    public String userID;

    public GetTimesPlayedByUserQuery(int clientZone, int clientNumber, long sendTime, String musicID, String userID) {
        super(clientZone, clientNumber, sendTime);
        this.musicID = musicID;
        this.userID = userID;
    }

    public GetTimesPlayedByUserResponse run(String filename, int serverZone) {
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

        return new GetTimesPlayedByUserResponse(clientNumber, clientZone, serverZone, counter);
    }

    @Override
    public String toString() {
        return "GetTimesPlayedByUserQuery(" + musicID + ", " + userID + ") clientZone: " + clientZone;
    }
}
