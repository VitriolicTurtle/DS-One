package Shared;

public class GetTopArtistsByUserGenreResponse extends Response {
    public String[] artists;

    /**
     * Constructs a new object.
     */
    public GetTopArtistsByUserGenreResponse(int zone, String[] artists) {
        super(zone);
        assert(artists.length == 3);
        this.artists = artists;
    }

    /**
     * Constructs a string representation of the response object.
     * @return: the response object in string representation.
     */
    @Override
    public String toString() {
        return "GetTopArtistsByUserGenreResponse('artists'=[" + artists[0] + ", " + artists[1] + ", " + artists[2] + "]) Zone: " + zone;
    }
}
