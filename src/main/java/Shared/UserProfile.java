package Shared;

import java.util.HashMap;

public class UserProfile {
    public String userID;
    public HashMap<String, HashMap<MusicProfile, Integer>> favoriteMusics;

    public UserProfile(String userID) {
        this.userID = userID;
        this.favoriteMusics = new HashMap<>();
    }
}
