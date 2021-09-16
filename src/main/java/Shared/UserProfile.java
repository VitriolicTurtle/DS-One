package Shared;

import java.util.HashMap;

public class UserProfile {
    public String UserID;
    public HashMap<String, MusicProfile> favoriteMusics = new HashMap<String, MusicProfile>();
}
