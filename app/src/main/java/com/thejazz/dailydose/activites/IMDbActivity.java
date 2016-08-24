package com.thejazz.dailydose.activites;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.thejazz.dailydose.R;
import com.thejazz.dailydose.utilities.UrlUtility;

public class IMDbActivity extends AppCompatActivity {

    private static String LOG_TAG = "IMDbActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imdb);
        String imdb_id = getIntent().getStringExtra("imdbId");
        WebView imdb = (WebView)findViewById(R.id.webview);
        imdb.getSettings().setJavaScriptEnabled(true);
        imdb.getSettings().setLoadWithOverviewMode(true);
        imdb.getSettings().setUseWideViewPort(true);
        imdb.setWebViewClient(new ourViewClient());
        String url = UrlUtility.buildIMDBUrl(imdb_id);
        imdb.loadUrl(url);
    }

    public class ourViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView v, String url)
        {
            v.loadUrl(url);
            return true;
        }
    }
}
