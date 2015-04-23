package edu.lclark.klc.klcradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Billy on 2015-04-05.
 */
public class PlayerService extends Service
        implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{
    private static final String TAG = "PlayerService";

    private AudioManager audioMgr;
    private MediaPlayer mp;
    private NotificationManager mgr;
    private PlayerReceiver playerReceiver;
    private Messenger hostMessenger = null;

    //////////////////////////////////////LIFECYCLE////////////////////////////////////////////////

    @Override
    public void onCreate() {
        Log.d(TAG, "in on create of service");
        super.onCreate();

        // Initialize MediaPlayer
        initMediaPlayer();
        mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        playerReceiver = new PlayerReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.PLAY_STREAM);
        intentFilter.addAction(Constants.PAUSE_STREAM);
        intentFilter.addAction(Constants.STOP_STREAM);
        // I register AudioManager.AUDIO_BECOMING_NOISY in manifest, is it bad to mix and match?
        registerReceiver(playerReceiver, intentFilter);
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

    //////////////////////////////////////BIND&MESSAGING////////////////////////////////////////////

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //communicate w/ hosting activity
                case Constants.MSG_MESSENGER:
                    hostMessenger = msg.replyTo;
                    break;
                case Constants.MSG_STATUS:
                    boolean status;
                    if (mp != null & mp.isPlaying()) {
//                        status = Boolean.TRUE;
                        status = true;
                    } else {
//                        status = Boolean.FALSE;
                        status = false;
                    }
                    Message reply = Message.obtain(null, Constants.MSG_STATUS, 0, 0, status);
                    try {
                        hostMessenger.send(reply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.MSG_PLAY:
                    Log.d(TAG, "playing from handler in service");
                    play();
                    break;
                case Constants.MSG_PAUSE:
                    pause();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public void sendMsg(int what) {
        Message msg = Message.obtain(null, what, 0, 0);
        try {
            hostMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////MEDIAPLAYER//////////////////////////////////////////////

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // when mediaPlayer finishes.. should never happen in final build
        Log.d(TAG, "onCompletion called");
        String message = "Stream failed";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        stop();
        stopSelf();
    }

    private void play() {
        Log.d(TAG, "in play method");

        // Get audio focus, return if not granted.
        // if we receive focus, play
        // because onAudioFocusChange is not called when first granted permission
        int result = audioMgr.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        Log.d(TAG, "requested focus");
        if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Log.e(TAG, "failed to get audio focus");
            // TODO is it best to just return here?
            return;
        }

        if (mp == null) initMediaPlayer();
        if (!mp.isPlaying()) {
            mp.start();
            sendMsg(Constants.MSG_PLAY);
            startForeground(Constants.PLAYER_ID, buildPlayerNotification());
        }
    }

    private void pause() {

        Log.d(TAG, "pausing");

        audioMgr.abandonAudioFocus(this);

        if (mp != null && mp.isPlaying()) {
            mp.pause();
            sendMsg(Constants.MSG_PAUSE);
            stopForeground(false);
            mgr.notify(Constants.PLAYER_ID, buildPlayerNotification());

        }
        else {
            Log.d(TAG, "Trying to pause while not playing");
            // Something going on we don't want so stop foreground and let process kill service
            stopForeground(true);
        }
    }

    private void stop() {
        // We need to pause here because if the activity is still active while
        // stop() is called, then we will not progress into onDestroy() but still want to stop
        // playback. (Which happens definitively in onDestroy)
        pause();
        stopForeground(true);
        stopSelf();
    }

    private void initMediaPlayer() {
        try {
            mp = MediaPlayer.create(this, R.raw.easy); //TODO waiting on KLC for actual stream
            Log.d(TAG, "created new mp");
//            mp.prepare(); create() already prepares it.
            mp.setOnCompletionListener(this);
            Log.d(TAG, "mp created");
        } catch (Exception ioe){ //IOException ioe) {
            Log.e(TAG, "error playing stream " + ioe.getMessage());
            String error = "Error loading stream";
            Toast.makeText(this, error, Toast.LENGTH_LONG);
        }
    }

    private Notification buildPlayerNotification() {
        // Notification.MediaStyle style = new Notification.MediaStyle();
        // this above requires API 21... does not exist earlier?
        // style.setMediaSession() Here we can set the media session -- need to
        // figure out media sessions


        // The pending intent that will bring KLCActivity to foreground when notification clicked
        //TODO this is just restarting home page atm, but eventually will need to reload into last page
        Intent notifIntent = new Intent(this, KLCActivity.class);
        notifIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent
                .getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // STOP
        Intent stopIntent = new Intent(Constants.STOP_STREAM);
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

        if (mp != null && mp.isPlaying()) {
            // PAUSE
            Intent pauseIntent = new Intent(Constants.PAUSE_STREAM);
            PendingIntent pausePI = PendingIntent
                    .getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action pause = new NotificationCompat
                    .Action(R.drawable.ic_action_pause, getString(R.string.pause), pausePI);

            builder.addAction(pause);
        }
        else {
            // PLAY
            Intent playIntent = new Intent(Constants.PLAY_STREAM);
            PendingIntent playPI = PendingIntent
                    .getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action play = new NotificationCompat
                    .Action(R.drawable.ic_action_play, getString(R.string.play), playPI);

            builder.addAction(play);
        }
        builder.addAction(stop);

        return builder.build();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //TODO this regains focus even if you have been listening to something else for a long time
        // do we want this to happen? ~~prolly yeah
        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "received focus");
                if (mp == null) initMediaPlayer();
                if (!mp.isPlaying()) {
                    mp.start();
                    sendMsg(Constants.MSG_PLAY);
                }
                // This is max volume WITHIN STREAM_MUSIC volume.
                mp.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "lost focus");
                stopForeground(false);
                if (mp.isPlaying()) mp.stop();
                mp.reset();
                mp.release();
                mp = null;
                mgr.notify(Constants.PLAYER_ID, buildPlayerNotification());
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "loss focus transient");
                if (mp.isPlaying()) pause();
                break;

            case  AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "ducking");
                if (mp.isPlaying()) mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Constants.PLAY_STREAM)) {
                Log.d(TAG, "PLAY_STREAM received");
                play();
            }
            else if (action.equals(Constants.PAUSE_STREAM)) {
                Log.d(TAG, "PAUSE_STREAM received");
                pause();
            }
            else if (action.equals(Constants.STOP_STREAM)) {
                Log.d(TAG, "STOP_STREAM received");
                stop();
            }
            else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
            }
            else {
                Log.d(TAG, "Unrecognized action: " + action);
            }
        }
    }
}
