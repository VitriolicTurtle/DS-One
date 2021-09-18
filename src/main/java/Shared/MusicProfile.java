package Shared;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicProfile implements Serializable {
    public String musicID;
    public ArrayList<String> artists;

    public MusicProfile(String musicID, ArrayList<String> artists) {
        this.musicID = musicID;
        this.artists = artists;
    }
}
