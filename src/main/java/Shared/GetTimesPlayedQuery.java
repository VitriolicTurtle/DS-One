package Shared;

/**
 * Class that gives the number of times a musicID has been played in total based on the dataset.csv.
 */
public class GetTimesPlayedQuery extends Query {
    public String musicID;

    public GetTimesPlayedQuery(int zone, String musicID) {
        super(zone);
        this.musicID = musicID;
    }

    public void run(String filename) {
        ;
    }

    @Override
    public String toString() {
        return "GetTimesPlayedQuery(" + musicID + ") zone: " + zone;
    }

}
