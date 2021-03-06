package edu.lclark.klc.klcradio;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Displays KLC Umbrella homepage
 */
public class UmbrellaFragment extends WebViewFragment {

    private String url = "http://klc-theumbrella.tumblr.com/";

    protected static UmbrellaFragment newInstance() {
        UmbrellaFragment frag = new UmbrellaFragment();

        return frag;
    }


    public UmbrellaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = super.onCreateView(inflater, container, savedInstanceState);

        // Splice in our own settings and load url
        getWebView().getSettings().setJavaScriptEnabled(true);
        // This essentially stops the WebView from trying to open browser on clicks.
        // I don't have to create my own because default behavior is to just load any clicked URLS
        // into the WebView here.
        getWebView().setWebViewClient(new WebViewClient());
        getWebView().loadUrl(url);

        return result;
    }


}
