package Shared;

public class GetTimesPlayedResponse extends Response {
    public int timesPlayed;

    /**
     * Constructs a new object.
     */
    public GetTimesPlayedResponse(int clientNumber, int clientZone, int serverZone, int timesPlayed) {
        super(clientNumber, clientZone, serverZone);
        this.timesPlayed = timesPlayed;
    }

    /**
     * Constructs a string representation of the response object.
     * @return: the response object in string representation.
     */
    @Override
    public String toString() {
        return "GetTimesPlayedResponse('timesPlayed'=" + timesPlayed + ")" +
                "clientNumber: " + clientNumber +
                ", clientZone: " + clientZone +
                ", serverZone: " + serverZone;
    }
}
