package Server;

import Shared.MusicProfile;
import Shared.Query;
import Shared.Response;
import Shared.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




public class ServerQueryProcessor implements Runnable {
    Server server;
    String filename;
    List<MusicProfile> cachedMusic = new ArrayList();
    List<UserProfile> cachedUsers = new ArrayList();

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

        // HARDCODED TEMPORARY ADDED USER:
        UserProfile tU = new UserProfile();
        MusicProfile mU = new MusicProfile();
        mU.musicID = "MghDT6bdDT";
        mU.Artists = "AfmxYc67c7";
        tU.UserID = "UFmWNV9BD0";
        tU.favoriteMusics.put("Metal", mU);
        cachedUsers.add(tU);
        /////////////////////////////////////////////////

        Query currentQuery = null;
        while (true) {
            currentQuery = this.server.fetchQuery();
            if (currentQuery != null) {
                Response response = currentQuery.run(filename);
                System.out.println(response);
                /*
                Response cachedResponse = currentQuery.cachedRun(cachedMusic, cachedUsers);
                System.out.println(cachedResponse);
                if(cachedResponse == null) {
                    Response response = currentQuery.run(filename);
                */
            }
        }

    }
}
