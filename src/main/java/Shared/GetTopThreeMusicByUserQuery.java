package Shared;

/**
 * Class that gives the top 3 musicIDs a users has listened to based on the dataset.csv.
 */
public class GetTopThreeMusicByUserQuery extends Query {
    public String userID;

    public GetTopThreeMusicByUserQuery(int zone, String userID) {
        super(zone);
        this.userID = userID;
    }

    public void run(String filename) {
        ;
    }

    @Override
    public String toString() {
        return "GetTopThreeMusicByUserQuery(" + userID + ") zone: " + zone;
    }
}
