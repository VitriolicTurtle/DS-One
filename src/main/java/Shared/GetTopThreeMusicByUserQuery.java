package Shared;

import java.io.File;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that gives the top 3 musicIDs a users has listened to based on the dataset.csv.
 */
public class GetTopThreeMusicByUserQuery extends Query {
    // Query arguments
    public String userID;

    // Query results
    public String[] result;

    /**
     * GetTopThreeMusicByUser query constructor. The client zone and number of the client sending the query,
     * as well as the arguments for the query, are all determined upon creating the query object.
     *
     * @param clientZone: the zone of the client sending the query.
     * @param clientNumber: the (address) number of the client sending the query.
     * @param userID: the userID argument for the query.
     */
    public GetTopThreeMusicByUserQuery(int clientZone, int clientNumber, String userID) {
        super(clientZone, clientNumber);
        this.userID = userID;
    }

    @Override
    public void run(String filename) {
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

        result = topThreeMusic;
    }

    @Override
    public String toString() {
        String s = "Top 3 musics for user '" + userID + "' were [" + result[0] + ", " + result[1] + ", " + result[2] + "]. ";
        s += "(Turnaround time: " + (timeStamps[4] - timeStamps[0]) + "ms, ";
        s += "execution time: " + (timeStamps[3] - timeStamps[2]) + "ms, ";
        s += "waiting time: " + (timeStamps[2] - timeStamps[1]) + "ms, ";
        s += "processed by server: " + processingServer + ")";
        return s;
    }
}
