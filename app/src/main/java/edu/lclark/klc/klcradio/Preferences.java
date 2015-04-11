package edu.lclark.klc.klcradio;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by Billy on 2015-04-02.
 */
public class Preferences extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

}
