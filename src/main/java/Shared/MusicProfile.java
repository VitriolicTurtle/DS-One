package Shared;

import java.util.ArrayList;

public class MusicProfile {
    public String musicID;
    public ArrayList<String> artists;

    public MusicProfile(String musicID, ArrayList<String> artists) {
        this.musicID = musicID;
        this.artists = artists;
    }
}
