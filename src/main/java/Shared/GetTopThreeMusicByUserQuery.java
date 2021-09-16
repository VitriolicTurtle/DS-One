package Shared;

import java.io.File;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * Class that gives the top 3 musicIDs a users has listened to based on the dataset.csv.
 */
public class GetTopThreeMusicByUserQuery extends Query {
    public String userID;

    public GetTopThreeMusicByUserQuery(int clientZone, int clientNumber, long sendTime, String userID) {
        super(clientZone, clientNumber, sendTime);
        this.userID = userID;
    }

    /**
     *
     * @param filename
     */
    public GetTopThreeMusicByUserResponse run(String filename, int serverZone) {
        Scanner scanner = null;
        HashMap<String, Integer> playCounts = new HashMap<String, Integer>();

        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains(userID)) { continue; }

            String[] data = line.split(",");
            String music = data[0];
            int timesPlayed = Integer.parseInt(data[data.length - 1]);
            playCounts.put(music, (playCounts.containsKey(music) ? playCounts.get(music) + timesPlayed : timesPlayed));
        }

        String[] topThreeMusic = new String[3];
        for (int i = 0; i < 3; i++) {
            Map.Entry<String, Integer> topEntry = null;
            for (Map.Entry<String, Integer> entry : playCounts.entrySet()) {
                topEntry = (topEntry == null || entry.getValue().compareTo(topEntry.getValue()) > 0) ?  entry : topEntry;
            }
            playCounts.remove(topEntry.getKey());
            topThreeMusic[i] = topEntry.getKey();
        }

        return new GetTopThreeMusicByUserResponse(clientNumber, clientZone, serverZone, topThreeMusic);
    }

    @Override
    public String toString() {
        return "GetTopThreeMusicByUserQuery(" + userID + ") zone: " + clientZone;
    }
}
