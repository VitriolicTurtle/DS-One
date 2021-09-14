package Server;

import Shared.Query;

public class ServerProcessTread implements Runnable {
    Server server;
    String filename;

    /**
     *
     * @param server
     * @param filename
     */
    public ServerProcessTread(Server server, String filename) {
        this.server = server;
        this.filename = filename;
    }

    /**
     *
     */
    @Override
    public void run() {
        Query currentQuery = null;
        while (true) {
            currentQuery = this.server.fetchQuery();
            if (currentQuery != null) {
                currentQuery.run(filename);
                System.out.println(currentQuery);
            }
        }

    }
}
