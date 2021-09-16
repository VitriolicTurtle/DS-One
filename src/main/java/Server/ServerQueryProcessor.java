package Server;

import Shared.Query;
import Shared.Response;

public class ServerQueryProcessor implements Runnable {
    Server server;
    String filename;
    int serverZone;

    /**
     * Constructor for a server query processor which will continuously process queries found in a server's query queue.
     *
     * @param server:   a reference to the server object containing the query queue.
     * @param filename: the filename of the dataset file necessary to process the query.
     */
    public ServerQueryProcessor(Server server, String filename, int serverZone) {
        this.server = server;
        this.filename = filename;
        this.serverZone = serverZone;
    }

    /**
     * Adds latency based on zone.
     * @param currentQuery Current Query sent by Client
     * @throws InterruptedException Thread interference.
     */
    public void checkConnectedZone(Query currentQuery) throws InterruptedException {
        if(currentQuery.clientZone == this.serverZone) {
            Thread.sleep(80);
        } else {
            Thread.sleep(170);
        }
    }


    /**
     * Continuously checks whether there are any queries waiting in the server's query queue, and fetches and processes
     * any queries found.
     */
    @Override
    public void run() {
        Query currentQuery = null;
        while (true) {
            currentQuery = this.server.fetchQuery();

            if (currentQuery != null) {
                try {
                    checkConnectedZone(currentQuery);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Response response = currentQuery.run(filename, serverZone);

            }
        }

    }
}
