package Shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserProfile implements Serializable {
    public String userID;
    public HashMap<String, HashMap<MusicProfile, Integer>> favoriteMusics;

    public UserProfile(String userID) {
        this.userID = userID;
        this.favoriteMusics = new HashMap<>();
    }

    @Override
    public String toString() {
        String s = "UserProfile object {\n";
        s += "\tuserID=" + userID + "\n";
        s += "\tfavoriteMusics={\n";
        for (Map.Entry<String, HashMap<MusicProfile, Integer>> genreEntry : favoriteMusics.entrySet()) {
            s += "\t\tGenre=" + genreEntry.getKey() +
                    ", HashMap={";
            for (Map.Entry<MusicProfile, Integer> musicEntry : genreEntry.getValue().entrySet()) {
                s += musicEntry.getKey().toString() + ": " + musicEntry.getValue() + ", ";
            }
            s += "]}\n";
        }
        s += "\t}\n}";
        return s;
    }
}
