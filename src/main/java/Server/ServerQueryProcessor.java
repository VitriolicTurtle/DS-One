package Server;

import Shared.Query;
import Shared.Response;

public class ServerQueryProcessor implements Runnable {
    Server server;
    String filename;

    /**
     * Constructor for a server query processor which will continuously process queries found in a server's query queue.
     * @param server: a reference to the server object containing the query queue.
     * @param filename: the filename of the dataset file necessary to process the query.
     */
    public ServerQueryProcessor(Server server, String filename) {
        this.server = server;
        this.filename = filename;
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
                Response response = currentQuery.run(filename);
                System.out.println(response);
            }
        }

    }
}
