package Server;

import Shared.Query;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void sendQuery(Query query) throws RemoteException;

    /**
     * Given a music id, return the number of times that it was played by all the user
     * @param musicID
     * @return
     * @throws RemoteException
     */
    int getTimesPlayed(String musicID) throws RemoteException;

    /**
     * Given a music id and a user id, return the number of times the music
     * was played by the user in total (considering all the records in the dataset
     * @param musicID
     * @param userID
     * @return
     * @throws RemoteException
     */
    int getTimesPlayedByUser(String musicID, String userID) throws RemoteException;

    /**
     * Given a userID, return a list of top three musics that were played most by that specific user.
     * The list should be sorted in ascending order from the user ranked 3rd to the user ranked 1st
     * @param userID
     * @return
     * @throws RemoteException
     */
    String[] getTopThreeMusicByUser(String userID) throws RemoteException;

    /**
     * Given a user ID and a genre, return a list of top three artists the user has listened to
     * @param userID
     * @param genre
     * @return
     * @throws RemoteException
     */
    String[] getTopArtistsByUserGenre(String userID, String genre) throws RemoteException;
}
