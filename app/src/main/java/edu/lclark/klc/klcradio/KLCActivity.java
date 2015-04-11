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

    public void onMainPlayerClick(View view) {
        showMiniPlayer();
        loadUmbrella();
    }

    private void loadUmbrella() {
        if (umbrella == null) {
            umbrella = UmbrellaFragment.newInstance();
        }

        getFragmentManager().beginTransaction().replace(R.id.main_content, umbrella).commit();
    }

    private void showMiniPlayer() {
        if (miniPlayer == null) {
            miniPlayer = MiniPlayerFragment.newInstance();
        }

        getFragmentManager().beginTransaction().add(android.R.id.content, miniPlayer).commit();
    }
}
