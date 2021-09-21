package Shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserProfile implements Serializable {
    public String userID;
    public HashMap<String, ArrayList<MusicProfile>> favoriteMusics = new HashMap<>();

    @Override
    public String toString() {
        String s = "UserProfile object {\n";
        s += "\tuserID=" + userID + "\n";
        s += "\tfavoriteMusics={\n";
        for (Map.Entry<String, ArrayList<MusicProfile>> genreEntry : favoriteMusics.entrySet()) {
            s += "\t\tGenre=" + genreEntry.getKey() +
                    ", HashMap={";
            for (MusicProfile musicEntry : genreEntry.getValue())
                s += musicEntry.toString() + ", ";
            s += "]}\n";
        }
        s += "\t}\n}";
        return s;
    }
}