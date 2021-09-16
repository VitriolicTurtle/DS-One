package Shared;

import java.io.Serializable;

public abstract class Query implements Serializable {
    public int clientZone;
    public int clientNumber;

    long sendTime;

    public Query(int clientZone, int clientNumber, long sendTime) {
        this.clientZone = clientZone;
        this.clientNumber = clientNumber;
        this.sendTime = sendTime;
    }
    public abstract Response run(String filename, int serverZone);
}
