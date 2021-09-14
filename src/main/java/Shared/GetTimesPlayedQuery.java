package Shared;

/**
 * Class that gives the number of times a musicID has been played in total based on the dataset.csv.
 */
public class GetTimesPlayedQuery extends Query {
    public String musicID;

    public GetTimesPlayedQuery(int zone, int clientNumber, String musicID) {
        super(zone, clientNumber);
        this.musicID = musicID;
    }

    public GetTimesPlayedResponse run(String filename) {
        return null;
    }

    @Override
    public String toString() {
        return "GetTimesPlayedQuery(" + musicID + ") zone: " + zone;
    }
}
