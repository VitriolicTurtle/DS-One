package Server;

import Shared.MusicProfile;
import Shared.Query;
import Shared.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerQueryProcessor implements Runnable {
    Server server;
    String filename;
    //List<MusicProfile> cachedMusic = new ArrayList();
    List<UserProfile> cachedUsers = new ArrayList();

    /**
     * Constructor for a server query processor which will continuously process queries found in a server's query queue.
     *
     * @param server:   a reference to the server object containing the query queue.
     * @param filename: the filename of the dataset file necessary to process the query.
     */
    public ServerQueryProcessor(Server server, String filename) {
        this.server = server;
        this.filename = filename;
    }

    /**
     * Adds latency based on zone.
     * @param currentQuery Current Query sent by Client
     * @throws InterruptedException Thread interference.
     */
    public void checkConnectedZone(Query currentQuery) throws InterruptedException {
        if(currentQuery.clientZone == server.getServerZone()) {
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

        // HARDCODED TEMPORARY ADDED USER:
        UserProfile tU = new UserProfile();
        UserProfile tU2 = new UserProfile();
        MusicProfile mU = new MusicProfile();
        MusicProfile mU2 = new MusicProfile();
        mU.musicID = "Mg5NsThkSC";
        mU.Artists[0].add("AfmxYc67c7");
        mU2.musicID = "MHAtRFWXh4";
        mU2.Artists[0].add("AGYuE8gTdf");
        tU.UserID = "ULOC2yXffF";
        tU2.UserID = "URPy97W1GA";
        HashMap<MusicProfile, Integer> tempHashMap = new HashMap<>();
        tempHashMap.put(mU, 9);
        HashMap<MusicProfile, Integer> tempHashMap2 = new HashMap<>();
        tempHashMap2.put(mU, 9);
        tempHashMap2.put(mU2, 9);
        tU.favoriteMusics.put("Metal", tempHashMap);
        tU2.favoriteMusics.put("Pop", tempHashMap2);
        cachedUsers.add(tU);
        cachedUsers.add(tU2);
        /////////////////////////////////////////////////

        Query currentQuery = null;
        while (true) {
            currentQuery = this.server.fetchQuery();

            // If no query object was returned, we continue waiting
            if (currentQuery == null) { continue; }

            try {
                checkConnectedZone(currentQuery);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If a query was fetched from the queue, we update the timestamp for this event before processing it
            currentQuery.timeStamps[2] = System.currentTimeMillis();

            // Run the query. This will populate the query result inside the query object
            if(currentQuery.serverCacheRun(cachedUsers) == false){
                currentQuery.run(filename);
            }

            // Update the timestamp reflecting the event of finishing the query processing
            currentQuery.timeStamps[3] = System.currentTimeMillis();

            // set the processingServer variable to reflect which server processed the query
            currentQuery.setProcessingServer(server.getServerZone());

            // Send the query (not populated with a response) back to the client
            server.sendResponse(currentQuery);
        }
    }
}
