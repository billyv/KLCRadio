package edu.lclark.klc.klcradio;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class MiniPlayerFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MiniPlayerFragment";

    // Height of player as percentage of screen, alpha of player
    private static final double PLAYER_HEIGHT = .075;
    private static final float ALPHA = 0.5f;
    private Button play;
    private Button pause;

    protected static MiniPlayerFragment newInstance() {
        MiniPlayerFragment frag = new MiniPlayerFragment();

        return frag;
    }


    public MiniPlayerFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "in onCreateView");
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_mini_player, container, false);

        // Get the window size
        Display display = ((Activity)result.getContext()).getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        // Then set this frag to be PLAYER_HEIGHT % of that size.
        ViewGroup.LayoutParams params = result.getLayoutParams();
        params.height = (int) (displaySize.y * PLAYER_HEIGHT);
        // Change some other params as well
//        result.setAlpha(ALPHA);

//        result.findViewById(R.id.mini_player_container);

        result.setLayoutParams(params);

        play = (Button) result.findViewById(R.id.play);
        play.setOnClickListener(this);
        pause = (Button) result.findViewById(R.id.pause);
        pause.setOnClickListener(this);

        return result;
    }

    @Override
    public void onAttach(Activity host) {
        super.onAttach(host);
    }

    @Override
    public void onClick(View v) {
        if (v == play) {
            Log.d(TAG, "starting");
            Intent serv = new Intent(getActivity(), PlayerService.class);
            getActivity().startService(serv);
            Intent broadcast = new Intent();
            broadcast.setAction(PlayerService.PLAY_STREAM);
            getActivity().sendBroadcast(broadcast);
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
        }
        else if (v == pause) {
            Log.d(TAG, "pausing");
            Intent broadcast = new Intent();
            broadcast.setAction(PlayerService.PAUSE_STREAM);
            getActivity().sendBroadcast(broadcast);
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
        }

    }


}
