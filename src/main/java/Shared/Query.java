package Shared;

import java.io.Serializable;

public abstract class Query implements Serializable {
    public int zone;

    public Query(int zone) {
        this.zone = zone;
    }
    public abstract void run(String filename);
}
