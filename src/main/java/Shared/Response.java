package Shared;

import java.io.Serializable;

public class Response implements Serializable {
    public UserProfile userProfile = null;
    public MusicProfile musicProfile = null;
    public int plays = 0;
}