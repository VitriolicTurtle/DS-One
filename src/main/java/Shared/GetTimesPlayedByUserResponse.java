package Shared;

public class GetTimesPlayedByUserResponse extends Response {
    public int timesPlayed;

    /**
     * Constructs a new object.
     */
    public GetTimesPlayedByUserResponse(int zone, int clientNumber, int timedPlayed) {
        super(zone, clientNumber);
        this.timesPlayed = timedPlayed;
    }

    /**
     * Constructs a string representation of the response object.
     * @return: the response object in string representation.
     */
    @Override
    public String toString() {
        return "GetTimesPlayedByUserResponse('timesPlayed'=" + timesPlayed + ") Zone: " + zone;
    }
}
