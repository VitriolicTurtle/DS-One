package Shared;

public class GetTimesPlayedResponse extends Response {
    public int timesPlayed;

    /**
     * Constructs a new object.
     */
    public GetTimesPlayedResponse(int zone, int timesPlayed) {
        super(zone);
        this.timesPlayed = timesPlayed;
    }

    /**
     * Constructs a string representation of the response object.
     * @return: the response object in string representation.
     */
    @Override
    public String toString() {
        return "GetTimesPlayedResponse('timesPlayed'=" + timesPlayed + ") Zone: " + zone;
    }
}
