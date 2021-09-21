package Server.ExecutionServer;

import Shared.*;

public class ExecutionServerProcessor implements Runnable {
    ExecutionServer server;
    String filename;
    Boolean serverCaching;

    /**
     * Constructor for a server query processor which will continuously process queries found in a server's query queue.
     *
     * @param server:   a reference to the server object containing the query queue.
     * @param filename: the filename of the dataset file necessary to process the query.
     */
    public ExecutionServerProcessor(ExecutionServer server, String filename, Boolean serverCaching) {
        this.server = server;
        this.filename = filename;
        this.serverCaching = serverCaching;
    }

    /**
     * Adds latency based on zone.
     *
     * @param currentQuery Current Query sent by Client
     * @throws InterruptedException Thread interference.
     */
    public void checkConnectedZone(Query currentQuery) throws InterruptedException {
        if (currentQuery.clientZone == server.getServerZone()) {
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

            // If no query object was returned, we continue waiting for the queue to fill
            if (currentQuery == null)
                continue;

            try {
                checkConnectedZone(currentQuery);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If a query was fetched from the queue, we update the timestamp for this event before processing it
            currentQuery.timeStamps[2] = System.currentTimeMillis();

            // Check if we can resolve the query from cache
            boolean cacheHit = false;
            if (serverCaching) {
                Response response = server.fetchCache(currentQuery);

                if (response != null) {
                    currentQuery.response = response;
                    cacheHit = true;
                }
            }

            // Run the query. This will populate the query result inside the query object
            if (!cacheHit)
                currentQuery.run(filename, server);

            // Update the timestamp reflecting the event of finishing the query processing
            currentQuery.timeStamps[3] = System.currentTimeMillis();

            // set the processingServer variable to reflect which server processed the query
            currentQuery.setProcessingServer(server.getServerZone());

            // Send the query (not populated with a response) back to the client
            System.out.println("server_" + server.getServerZone() + "-processing thread finished processing query.");
            server.sendResponse(currentQuery);
        }
    }
}