package Shared;

import java.io.Serializable;

public abstract class Query implements Serializable {
    public int zone;
    public int clientNumber;

    public Query(int zone, int clientNumber) {
        this.zone = zone;
        this.clientNumber = clientNumber;
    }
    public abstract Response run(String filename);
}
