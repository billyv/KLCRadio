package edu.lclark.klc.klcradio;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

//TODO "KLC Radio has stopped working" coming up randomly even after app has been closed long ago
// this hasn't happened in a bit -- possibly fixed by other changes? keep eye out

//TODO calendar portion of app
//TODO provide basic browser tools for WebView fragment -- back + refresh.
//TODO tumblr, add progress bar and remove Tumblr menu
//TODO art + styling

public class KLCActivity extends Activity {
    private static final String TAG = "KLCActivity";

    // Move these tags to constants class?
    private final static String PLAYER = "player";
    private final static String UMBRELLA = "Umbrella";
    private final static String CALENDAR = "Calendar";
    private final static String HOME = "Home";
    private PlayerFragment player;
    private UmbrellaFragment umbrella;
    private CalendarFragment calendar;
    private String[] navItems;
    private DrawerLayout drawerLayout;
    private ListView navDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_klc);

        title = (String) getTitle();

        player = (PlayerFragment) getFragmentManager().findFragmentByTag(PLAYER);
        umbrella = (UmbrellaFragment) getFragmentManager().findFragmentByTag(UMBRELLA);
        calendar = (CalendarFragment) getFragmentManager().findFragmentByTag(CALENDAR);

        // Get Navigation Drawer items
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navDrawer = (ListView) findViewById(R.id.nav_drawer);
        navItems = getResources().getStringArray(R.array.nav_items);

        // Set adapter for nav drawer's list view
        navDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, navItems));
        navDrawer.setOnItemClickListener(new DrawerItemClickListener());

        // Create a drawer toggle and set it as our DrawerListener
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when drawer totally closed **/
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(title); // current frag title when closed
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when drawer totally opened **/
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getActionBar().setTitle(getTitle()); // app title when opened
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        // TODO find better way to open app later -- like a home screen of just stream
        showUmbrella();
        showPlayer();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_klc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Called whenever invalidateOptionsMenu() is called **/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide certain option items
        //TODO
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Otherwise handle normally

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, Preferences.class);
            startActivity(i);

            return true;
        }
        else if (id == R.id.home) {
            // TODO do something for home?
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUmbrella() {
        if (umbrella == null) {
            umbrella = UmbrellaFragment.newInstance();
        }
        getFragmentManager().beginTransaction().replace(R.id.main_content, umbrella).commit();
    }

    private void showPlayer() {
        if (player == null) {
            player = PlayerFragment.newInstance();
        }
        getFragmentManager().beginTransaction().replace(R.id.player, player).commit();
        Log.d(TAG, "showed player");
    }

    private void selectItem(int position) {
        String fragType = navItems[position];
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Find fragment to load and replace previous content.
        if (fragType.equals(CALENDAR)) {
            if (calendar == null) {
                calendar = CalendarFragment.newInstance();
            }
            transaction.replace(R.id.main_content, calendar);
        }
        else if (fragType.equals(UMBRELLA)) {
            if (umbrella == null) {
                umbrella = UmbrellaFragment.newInstance();
            }
            transaction.replace(R.id.main_content, umbrella);
        }
        transaction.commit();

        // Highlight selected item, update title, and close drawer
        navDrawer.setItemChecked(position, true);
        title = navItems[position];
        getActionBar().setTitle(title);
        drawerLayout.closeDrawer(navDrawer);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}


