package Shared;

import java.io.Serializable;

public abstract class Response implements Serializable {
    public int zone;

    public Response(int zone, int clientNumber) {
        this.zone = zone;
    }
}
