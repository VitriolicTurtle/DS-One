package Shared;

public class GetTimesPlayedByUserResponse extends Response {
    public int timesPlayed;

    /**
     * Constructs a new object.
     */
    public GetTimesPlayedByUserResponse(int clientNumber, int clientZone, int serverZone, int timesPlayed) {
        super(clientNumber, clientZone, serverZone);
        this.timesPlayed = timesPlayed;
    }

    /**
     * Constructs a string representation of the response object.
     * @return: the response object in string representation.
     */
    @Override
    public String toString() {
        return "GetTimesPlayedByUserResponse('timesPlayed'=" + timesPlayed + ")" +
                "clientNumber: " + clientNumber +
                ", clientZone: " + clientZone +
                ", serverZone: " + serverZone;
    }
}
