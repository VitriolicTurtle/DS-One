package Shared;

import java.io.Serializable;

import Server.ExecutionServer.ExecutionServer;

public abstract class Query implements Serializable {
    // Which zone is the client sending the query in
    public int clientZone;

    // Which client object is sending the query (used to identify where to send the response)
    public int clientNumber;

    // Which server is processing the query
    public int processingServer;

    // Query response
    public Response response = null;

    /**
     * Used to store the following timestamps in the following indices
     * idx - timestamp
     * 0 - when the query is sent from the client to the server for processing
     * 1 - when the query is added to the processing queue in the server
     * 2 - when the query is fetched from the waiting queue by the processing thread in the server;
     * This also marks the start of the query being processed by the server
     * 3 - when the server finishes processing the query
     * 4 - when the query response arrives back in the client object
     */
    public long[] timeStamps = new long[5];

    public Query(int clientZone, int clientNumber) {
        this.clientZone = clientZone;
        this.clientNumber = clientNumber;
    }

    // Setters
    public void setClientZone(int clientZone) {
        this.clientZone = clientZone;
    }

    public void setClientNumber(int clientNumber) {
        this.clientNumber = clientNumber;
    }

    public void setProcessingServer(int processingServer) {
        this.processingServer = processingServer;
    }

    // Getters
    public int getClientZone() {
        return this.clientZone;
    }

    public int getClientNumber() {
        return this.clientNumber;
    }

    public int getProcessingServer() {
        return this.processingServer;
    }

    public abstract void run(String filename, ExecutionServer server);

    public abstract String getQueryString();
    public abstract String getHashString();

    @Override
    public String toString() {
        return "Abstract query object";
    }
}