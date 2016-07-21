package com.thejazz.dailydose;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static android.support.v4.view.MenuItemCompat.getActionProvider;

public class TvDetailActivity extends AppCompatActivity {

    public TextView showInfo;
    private String TvShowDetailText;
    private ShareActionProvider mShareActionProvider;
    private ArrayList<HashMap<String,String>[]> mylist = new ArrayList<HashMap<String,String>[]>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = this.getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            TvShowDetailText = intent.getStringExtra(Intent.EXTRA_TEXT);
            showInfo = (TextView) findViewById(R.id.detail_tv);
            showInfo.setText(TvShowDetailText);
        }
    }

    private Intent createShareIntent(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "NEXT EPISODE:\n"+TvShowDetailText+" #IdiotBoxAPP");
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }else
            Log.e("ShareActionProvider", "ShareActionProvider is null");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh){
            FetchShowTask showTask = new FetchShowTask();
            showTask.execute();
            return true;
        }
        if(id == R.id.action_share ){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}