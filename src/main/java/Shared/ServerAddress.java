package Shared;

import java.io.Serializable;

public class ServerAddress implements Serializable {
    public String address = null;
    public int port = -1;

    /**
     * Stores an address and port for a server.
     * This is used to send the necessary data from the proxy-server to clients describing which server -
     * the client should connect to.
     * @param address: the server address name.
     * @param port: the port the server is running on.
     */
    public ServerInfo(String address, int port) {
        this.address = address;
        this.port = port;
    }
}
