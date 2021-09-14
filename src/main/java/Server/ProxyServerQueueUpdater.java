package Server;

public class ProxyServerQueueUpdater implements Runnable {
    private ProxyServer proxyServer;
    private int serverZone;

    /**
     * Constructor for a proxy-server server queue updater.
     * @param proxyServer: a reference to the proxy-server.
     * @param serverZone: the zone of the server we are updating the proxy-server's local queue load data for.
     */
    public ProxyServerQueueUpdater(ProxyServer proxyServer, int serverZone) {
        this.proxyServer = proxyServer;
        this.serverZone = serverZone;
    }

    /**
     * Updates the proxy-server's local entry for the (processing) server's queue size.
     * Once it has updated the proxy-server's local size for the server's queue, the thread will die.
     */
    @Override
    public void run() {
        try {
            proxyServer.updateQueueData(serverZone);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
