package Shared;

import java.io.Serializable;

public abstract class Response implements Serializable {
    public int clientNumber;
    public int clientZone;
    public int serverZone;

    public Response(int clientNumber, int clientZone, int serverZone) {
        this.clientNumber = clientNumber;
        this.clientZone = clientZone;
        this.serverZone = serverZone;
    }
}