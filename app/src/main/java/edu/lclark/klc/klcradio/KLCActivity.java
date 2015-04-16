package edu.lclark.klc.klcradio;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

//TODO STOP SPAWNING A BILLION ACTIVITIES FROM CLICKING NOTIFICATION OVER AND OVER

public class KLCActivity extends Activity {

    private final static String MINI_PLAYER = "miniplayer";
    private final static String UMBRELLA = "umbrella";
    private MiniPlayerFragment miniPlayer = null;
    private UmbrellaFragment umbrella = null;

    private static final String TAG = "KLCActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_klc);

        miniPlayer = (MiniPlayerFragment) getFragmentManager().findFragmentByTag(MINI_PLAYER);
        umbrella = (UmbrellaFragment) getFragmentManager().findFragmentByTag(UMBRELLA);

        // TODO find better way to open app later

        //TODO this isn't the right way to change display of playPause
        // rather, lets implement it using broadcasts? event bus?
        // also, let's create a constants class
        showMiniPlayer();
        showUmbrella();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_klc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, Preferences.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUmbrella() {
        if (umbrella == null) {
            umbrella = UmbrellaFragment.newInstance();
        }

        getFragmentManager().beginTransaction().replace(R.id.main_content, umbrella).commit();
    }

    private void showMiniPlayer() {
        if (miniPlayer == null) {
            miniPlayer = MiniPlayerFragment.newInstance();
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, miniPlayer).commit();
    }
}
