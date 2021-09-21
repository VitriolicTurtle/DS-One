package Shared;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicProfile implements Serializable {
    public String musicID;
    public ArrayList<String> artists;

    @Override
    public String toString() {
        String s = "[musicID=" + musicID + ", artists=( ";
        for (String artist : artists)
            s += artist + " ";
        s += ")]";
        return s;
    }
}