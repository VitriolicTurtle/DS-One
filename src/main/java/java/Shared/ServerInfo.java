package java.Shared;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    public String address = null;
    public int port = -1;

    public ServerInfo(String address, int port) {
        this.address = address;
        this.port = port;
    }
}
