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

    public GetTopThreeMusicByUserQuery(int zone, String userID) {
        super(zone);
        this.userID = userID;
    }

    /**
     *
     * @param filename
     */
    public void run(String filename) {
        System.out.println("GetTopThreeMusicByUserQuery from server_" + this.zone);
        Scanner scanner = null;
        HashMap<String, Integer> topSongsMap = new HashMap<String, Integer>();
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        while (scanner.hasNextLine()) {
            int userIndex = 3;                                                 // Smallest index for user is 3 because there is always minimum 1 artist
            String line = scanner.nextLine();
            if(line.contains(userID)) {
                String[] data = line.split(",");
                while (!data[userIndex].startsWith("U")) {                     // If there are more artists than 1, loop through indexes to find user.
                    userIndex++;
                }
                if(topSongsMap.containsKey(data[0])){                          // If song is already in the Hashmap, add times listened to keys value.
                    topSongsMap.put(data[0], topSongsMap.get(data[0]) + Integer.parseInt(data[userIndex+1]));
                } else {                                                       //  Else, add the new key and its value.
                    topSongsMap.put(data[0], Integer.parseInt(data[userIndex+1]));
                }

            }
        }
        Object[] topSongsArray = topSongsMap.entrySet().toArray();              // Array object created to sort HashMap by value.
        Arrays.sort(topSongsArray, new Comparator(){                            // Sorts the array based on custom comparator function.
            public int compare(Object val1, Object val2){
                return((Map.Entry<String, Integer>) val2).getValue().compareTo(((Map.Entry<String, Integer>) val1).getValue());
            }
        });

        for(Object s : topSongsArray){
            System.out.println(((Map.Entry<String, Integer>) s).getKey() + " - Played: " + ((Map.Entry<String, Integer>) s).getValue());
        }
    }

    @Override
    public String toString() {
        return "GetTopThreeMusicByUserQuery(" + userID + ") zone: " + zone;
    }
}
