package edu.lclark.klc.klcradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Billy on 2015-04-05.
 */
public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "PlayerService";

    private static final String EXTRA_STREAM = "EXTRA_SONG";
    public static final String INTENT_BASE_NAME = "edu.lclark.klc.klcradio.PlayerService";
    public static final String PLAY_STREAM = INTENT_BASE_NAME + ".PLAY_STREAM";
    public static final String PAUSE_STREAM = INTENT_BASE_NAME + ".PAUSE_STREAM";
    public static final String STOP_STREAM = INTENT_BASE_NAME + ".STOP_STREAM";
    public static final int PLAYER_ID = 23;
    private MediaPlayer mp;
    private NotificationManager mgr;
    private PlayerReceiver playerReceiver;

    @Override
    public void onCreate() {
        Log.d(TAG, "in on create");
        super.onCreate();

        // Initialize MediaPlayer
        mp = MediaPlayer.create(this, R.raw.easy);
        mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        playerReceiver = new PlayerReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY_STREAM);
        intentFilter.addAction(PAUSE_STREAM);
        intentFilter.addAction(STOP_STREAM);
        registerReceiver(playerReceiver, intentFilter);

        play();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
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
            // We want to be in foreground while playing.
            startForeground(PLAYER_ID, buildPlayerNotification());
            return;
        }
        else if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }

        try {
            mp = MediaPlayer.create(this, R.raw.easy);
            Log.d(TAG, "created new mp");
//            mp.prepare(); create() already prepares it.
            mp.start();
            mp.setOnCompletionListener(this);
            startForeground(PLAYER_ID, buildPlayerNotification());
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
            stopForeground(false);
            mgr.notify(PLAYER_ID, buildPlayerNotification());

        }
        else if (mp != null && !mp.isPlaying()) {
            Log.d(TAG, "Trying to pause while not playing");
            // Something going on we don't want so stop foreground and let process kill service
            stopForeground(true);
        }
    }

    private void stop() {
        stopForeground(true);
        stopSelf();
    }

    private Notification buildPlayerNotification() {
        // Notification.MediaStyle style = new Notification.MediaStyle();
        // this above requires API 21... how to do before?
        // style.setMediaSession() Here we can set the media session -- need to
        // figure out media sessions

        // The pending intent that will bring KLCActivity to foreground when notification clicked
        Intent intent = new Intent(this, KLCActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // PAUSE
        Intent pauseIntent = new Intent(PAUSE_STREAM);
        PendingIntent pausePI = PendingIntent
                .getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action pause = new NotificationCompat
                .Action(R.drawable.ic_action_pause, getString(R.string.pause), pausePI);

        // PLAY
        Intent playIntent = new Intent(PLAY_STREAM);
        PendingIntent playPI = PendingIntent
                .getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action play = new NotificationCompat
                .Action(R.drawable.ic_action_play, getString(R.string.play), playPI);

        // STOP
        Intent stopIntent = new Intent(STOP_STREAM);
        PendingIntent stopPI = PendingIntent
                .getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action stop = new NotificationCompat
                .Action(R.drawable.ic_action_cancel, getString(R.string.stop), stopPI);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_settings) // TODO: placeholder icon
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_detail))
                .setContentIntent(pi);

        if (mp.isPlaying()) {
            builder.addAction(pause);
        }
        else {
            builder.addAction(play);
        }
        builder.addAction(stop);

        return builder.build();
    }

    private class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(PLAY_STREAM)) {
                Log.d(TAG, PLAY_STREAM + " received");
                play();
            }
            else if (action.equals(PAUSE_STREAM)) {
                Log.d(TAG, PAUSE_STREAM + " received");
                pause();
            }
            else if (action.equals(STOP_STREAM)) {
                Log.d(TAG, STOP_STREAM + " received");
                stop();
            }
            else {
                Log.d(TAG, "Unrecognized action: " + action);
            }
        }
    }
}
