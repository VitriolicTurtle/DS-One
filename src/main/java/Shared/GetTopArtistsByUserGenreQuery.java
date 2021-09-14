package Shared;

/**
 * Class that gives the top 3 artists a specific user ID has listened to based on genre provided based on dataset.csv.
 */
public class GetTopArtistsByUserGenreQuery extends Query {
    public String userID;
    public String genre;

    public GetTopArtistsByUserGenreQuery(int zone, String userID, String genre) {
        super(zone);
        this.userID = userID;
        this.genre = genre;
    }

    public void run(String filename) {
        ;
    }

    @Override
    public String toString() {
        return "GetTopArtistsByUserGenreQuery(" + userID + ", " + genre + ") zone: " + zone;
    }
}
