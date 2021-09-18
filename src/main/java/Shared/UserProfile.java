package Shared;

import java.io.Serializable;
import java.util.HashMap;

public class UserProfile implements Serializable {
    public String userID;
    public HashMap<String, HashMap<MusicProfile, Integer>> favoriteMusics;

    public UserProfile(String userID) {
        this.userID = userID;
        this.favoriteMusics = new HashMap<>();
    }
}
