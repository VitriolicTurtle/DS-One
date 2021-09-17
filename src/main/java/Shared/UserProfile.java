package Shared;

import java.util.HashMap;

public class UserProfile {
    public String UserID;
    public HashMap<String, HashMap<MusicProfile, Integer>> favoriteMusics = new HashMap<String, HashMap<MusicProfile, Integer>>();
}
