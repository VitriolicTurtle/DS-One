package Shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


public abstract class Query implements Serializable {
    public int zone;
    public int clientNumber;
    public String cacheKey;


    public Query(int zone, int clientNumber) {
        this.zone = zone;
        this.clientNumber = clientNumber;
    }
    public abstract Response run(String filename);

    public abstract Response cachedRun(List<MusicProfile> cachedMusic, List<UserProfile> cachedUsers);
}
