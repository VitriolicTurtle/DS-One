package Server;

public class ProxyServerThread implements Runnable{
    private ProxyServer proxyServer;
    private int serverZone;

    /**
     * Constructor for
     * @param proxyServer
     * @param serverZone
     */
    public ProxyServerThread(ProxyServer proxyServer, int serverZone) {
        this.proxyServer = proxyServer;
        this.serverZone = serverZone;
    }

    /**
     *
     */
    @Override
    public void run() {

    }
}
