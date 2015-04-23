package edu.lclark.klc.klcradio;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "PlayerFragment";

    private ToggleButton playPause; // if checked, pause icon shows.
    private boolean bound;
    private Messenger service = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service){
            PlayerFragment.this.service = new Messenger(service);
            Message msg = Message.obtain(null, Constants.MSG_MESSENGER, 0, 0);
            msg.replyTo = mMessenger;
            try {
                PlayerFragment.this.service.send(msg);
                Log.d(TAG, "message containing messenger sent to service");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Here we check whether the stream is currently playing, so our button is in proper state
            // the response gives us status, handled in our handler.
            msg = Message.obtain(null, Constants.MSG_STATUS, 0, 0);
            try {
                PlayerFragment.this.service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "service disconnected");
            service = null;
            bound = false;
        }
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_STATUS:
                    playPause.setChecked((boolean)msg.obj);
                    playPause.setVisibility(View.VISIBLE);
                    break;
                case Constants.MSG_PLAY:
                    Log.d(TAG, "received PLAY and bound is " + bound);
                    playPause.setChecked(true);
                    break;
                case Constants.MSG_PAUSE:
                    Log.d(TAG, "received PAUSE and bound is " + bound);
                    playPause.setChecked(false);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());


    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected static PlayerFragment newInstance() {
        PlayerFragment frag = new PlayerFragment();

        return frag;
    }


    public PlayerFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent serv = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(serv);
        getActivity().bindService(serv, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_player, container, false);

        // Get the window size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize); // puts size into our point

        // Then set this frag to be PLAYER_HEIGHT % of that size.
        ViewGroup.LayoutParams params = result.getLayoutParams();
        params.height = (int) (displaySize.y * Constants.PLAYER_HEIGHT);

        result.setLayoutParams(params);
        Log.d(TAG, "set height to " + params.height);

        playPause = (ToggleButton) result.findViewById(R.id.play_pause);
        playPause.setOnClickListener(this);

        return result;
    }

    @Override
    public void onStop() {
        getActivity().unbindService(mConnection);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "in onClick");
        if (v == playPause) {
            // this is the state AFTER click
            // so if on is true, it means we just went into Play mode
            Boolean on = ((ToggleButton)v).isChecked();

            if (on) {
                Log.d(TAG, "playing from frag");
                Message msg = Message.obtain(null, Constants.MSG_PLAY, 0, 0);
                try {
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.d(TAG, "pausing from frag");
                Message msg = Message.obtain(null, Constants.MSG_PAUSE, 0, 0);
                try {
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
