package Shared;

import java.io.File;
import java.util.*;

/**
 * Class that gives the top 3 artists a specific user ID has listened to based on genre provided based on dataset.csv.
 */
public class GetTopArtistsByUserGenreQuery extends Query {
    public String userID;
    public String genre;

    public GetTopArtistsByUserGenreQuery(int zone, int clientNumber, String userID, String genre) {
        super(zone, clientNumber);
        this.userID = userID;
        this.genre = genre;
    }

    public GetTopArtistsByUserGenreResponse run(String filename) {
        System.out.println("GetTopArtistsByUserGenreQuery from server_" + this.zone);
        Scanner scanner = null;
        HashMap<String, Integer> topArtistsMap = new HashMap<String, Integer>();
        try {
            scanner = new Scanner(new File(filename));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println("Something went wrong while trying to complete request.");
            System.exit(1);
        }

        while (scanner.hasNextLine()) {
            int artistIndex = 1;                                               // Smallest index for artist is 1 because there is always minimum 1 artist
            String line = scanner.nextLine();
            if(line.contains(this.userID) && line.contains(this.genre)) {
                String[] data = line.split(",");
                while (data[artistIndex].startsWith("A")) {                     // While there are more artists than 1, loop through them all.
                    if (topArtistsMap.containsKey(data[0])) {                    // If the artist is already in the map, add the listen time.
                        topArtistsMap.put(data[artistIndex], topArtistsMap.get(data[artistIndex]) + Integer.parseInt(data[data.length - 1]));
                    } else {                                                      // Else, add artist to hashmap
                        topArtistsMap.put(data[artistIndex], Integer.parseInt(data[data.length - 1]));
                    }
                    artistIndex++;
                }
            }
        }
        Object[] topArtistsArray = topArtistsMap.entrySet().toArray();              // Array object created to sort HashMap by value.
        Arrays.sort(topArtistsArray, new Comparator(){                              // Sorts the array based on custom comparator function.
            public int compare(Object val1, Object val2){
                return((Map.Entry<String, Integer>) val2).getValue().compareTo(((Map.Entry<String, Integer>) val1).getValue());
            }
        });

        String[] result = new String[3];
        for(int i = 0; i < 3; i++){                                          // Prints all objects in array.
            if(topArtistsArray[i] != null) {
                result[i] = i + ". " + "(" + this.userID + ", " + this.genre + ") " + ((Map.Entry<String, Integer>) topArtistsArray[i]).getKey() + " - " + ((Map.Entry<String, Integer>) topArtistsArray[i]).getValue() + " \n";
            }
        }
        return new GetTopArtistsByUserGenreResponse(zone, clientNumber, result);
    }

    @Override
    public String toString() {
        return "GetTopArtistsByUserGenreQuery(" + userID + ", " + genre + ") zone: " + zone;
    }
}
