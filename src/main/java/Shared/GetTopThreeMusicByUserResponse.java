package Shared;

public class GetTopThreeMusicByUserResponse extends Response {
    public String[] music;

    /**
     * Constructs a new object.
     */
    public GetTopThreeMusicByUserResponse(int zone, String[] music) {
        super(zone);
        assert(music.length == 3);
        this.music = music;
    }

    /**
     * Constructs a string representation of the response object.
     * @return: the response object in string representation.
     */
    @Override
    public String toString() {
        return "GetTopThreeMusicByUserResponse('music'=[" + music[0] + ", " + music[1] + ", " + music[2] + "]) Zone: " + zone;
    }
}
