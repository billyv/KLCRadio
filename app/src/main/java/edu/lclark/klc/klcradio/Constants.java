package edu.lclark.klc.klcradio;

/**
 * Created by Billy on 2015-04-18.
 */
public class Constants {
    public static final String INTENT_BASE_NAME = "edu.lclark.klc.klcradio.PlayerService";
    public static final String PLAY_STREAM = INTENT_BASE_NAME + ".PLAY_STREAM";
    public static final String PAUSE_STREAM = INTENT_BASE_NAME + ".PAUSE_STREAM";
    public static final String STOP_STREAM = INTENT_BASE_NAME + ".STOP_STREAM";
    public static final int PLAYER_ID = 23;
    public static final int MSG_MESSENGER = 1;
    public static final int MSG_PLAY = 2;
    public static final int MSG_PAUSE = 3;
    public static final int MSG_STATUS = 4;
    // Height of player as percentage of screen size
    public static final double PLAYER_HEIGHT = .075;
}
