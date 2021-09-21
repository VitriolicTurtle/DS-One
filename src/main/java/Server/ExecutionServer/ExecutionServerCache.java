package Server.ExecutionServer;

import Shared.Response;

import java.util.LinkedHashMap;

public class ExecutionServerCache {
    Integer musicProfileCount = 0;
    Integer userProfileCount = 0;

    LinkedHashMap<String, Response> cache = new LinkedHashMap();

    public ExecutionServerCache() {
        ;
    }

}
