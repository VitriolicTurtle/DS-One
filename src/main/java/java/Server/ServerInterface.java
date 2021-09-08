package java.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    // Given a music id, return the number of times that it was played by all the user
    public int getTimesPlayed(String musicID) throws RemoteException;

    /* Given a music id and a user id, return the number of times the music
    was played by the user in total (considering all the records in the dataset) */
    public int getTimesPlayedByUser(String musicID, String userID) throws RemoteException;

    /* Given a userID, return a list of top three musics that were played most by that specific user.
    The list should be sorted in ascending order from the user ranked 3rd to the user ranked 1st */
    public String[] getTopThreeMusicByUser(String userID) throws RemoteException;

    // Given a user ID and a genre, return a list of top three artists the user has listened to
    public String[] getTopArtistsByUserGenre(String userID, String genre) throws RemoteException;

    // OTHER

    public int getServerPort() throws RemoteException;
}
