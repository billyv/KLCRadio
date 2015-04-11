package edu.lclark.klc.klcradio;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Billy on 2015-04-05.
 */
public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "PlayerService";

    private static final String EXTRA_STREAM = "EXTRA_SONG";
    private static final String INTENT_BASE_NAME = "edu.lclark.klc.klcradio.PlayerService";
    public static final String PLAY_STREAM = INTENT_BASE_NAME + ".PLAY_STREAM";
    public static final String PAUSE_STREAM = INTENT_BASE_NAME + ".PAUSE_STREAM";
    public static final int PLAYER_ID = 23;
    private MediaPlayer mp = null;
    private PlayerReceiver playerReceiver = new PlayerReceiver();

    @Override
    public void onCreate() {
        Log.d(TAG, "in on create");

        super.onCreate();
        startForeground(PLAYER_ID, buildPlayerNotification());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY_STREAM);
        intentFilter.addAction(PAUSE_STREAM);
        registerReceiver(playerReceiver, intentFilter);

        play();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        mp = null;
        unregisterReceiver(playerReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // when mediaPlayer finishes.. should never happen
        Log.d(TAG, "onCompletion called");
        String message = "Stream failed";
        Toast.makeText(this, message, Toast.LENGTH_LONG);
        stopSelf();
    }

    private void play() {

        Log.d(TAG, "in play method");

        if (mp != null && !mp.isPlaying()) {
            mp.start();
            return;
        }
        else if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }

        try {
            mp = MediaPlayer.create(this, R.raw.easy);
            Log.d(TAG, "created new mp");
//            mp.prepare(); create() already prepares it.
            mp.start();
            mp.setOnCompletionListener(this);
        } catch (Exception ioe){ //IOException ioe) {
            Log.e(TAG, "error playing stream " + ioe.getMessage());
            String error = "Error loading stream";
            Toast.makeText(this, error, Toast.LENGTH_LONG);
        }

    }

    private void pause() {

        Log.d(TAG, "pausing");

        if (mp != null && mp.isPlaying()) {
            mp.pause();
        }
        else if (mp != null && !mp.isPlaying()) {
            Log.d(TAG, "Trying to pause while not playing");
        }

    }

    private Notification buildPlayerNotification() {
        return null;
    }

    private class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == PLAY_STREAM) {
                Log.d(TAG, PLAY_STREAM + " received");
                play();
            }
            else if (action == PAUSE_STREAM) {
                Log.d(TAG, PAUSE_STREAM + " received");
                pause();
            }
            else {
                Log.d(TAG, "Unrecognized action: " + action);
            }
        }
    }
}
