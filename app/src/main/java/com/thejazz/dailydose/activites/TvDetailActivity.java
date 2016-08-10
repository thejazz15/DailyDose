package com.thejazz.dailydose.activites;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.thejazz.dailydose.R;
import com.thejazz.dailydose.data.TvShowsContract.TvShowsEntry;

public class TvDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public TextView showNametv, epiNametv, airDatetv, seasontv, epiNumtv, networktv, countrytv, imgUrltv;

    private ShareActionProvider mShareActionProvider;
    private int episode_ID;

    private static final int DETAIL_LOADER_ID = 0;

    private static final String[] TV_SHOW_COLUMNS = {
            TvShowsEntry._ID,
            TvShowsEntry.COLUMN_SHOW_NAME,
            TvShowsEntry.COLUMN_EPISODE_NAME,
            TvShowsEntry.COLUMN_SEASON,
            TvShowsEntry.COLUMN_EPISODE_NUM,
            TvShowsEntry.COLUMN_AIR_DATE,
            TvShowsEntry.COLUMN_NETWORK,
            TvShowsEntry.COLUMN_COUNTRY_CODE,
            TvShowsEntry.COLUMN_IMG_URL
    };

    private static final int COL_ID = 0;
    private static final int COL_SHOW_NAME = 1;
    private static final int COL_EPI_NAME = 2;
    private static final int COL_SEASON = 3;
    private static final int COL_EPI_NUM = 4;
    private static final int COL_AIR_DATE = 5;
    private static final int COL_NETWORK = 6;
    private static final int COL_COUNTRY_CODE = 7;
    private static final int COL_IMG_URL = 8;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_detail);
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "NEXT EPISODE:\n" + " #IdiotBoxAPP");
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
        } else
            Log.e("ShareActionProvider", "ShareActionProvider is null");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            return true;
        }
        if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra("column_ID")) {
            episode_ID = intent.getIntExtra("column_ID", 0);
            Log.v("EPISODE_ID", Integer.toString(episode_ID));
        }
        Uri episodeUri = TvShowsEntry.buildShowUri(episode_ID);
        Log.v("EPISODE_URI", episodeUri.toString());
        return new CursorLoader(getApplicationContext(),
                episodeUri,
                TV_SHOW_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("CURSOR TEST", "Checking if data is found.");
        if (!data.moveToFirst()) {
            Log.v("CURSOR NO DATA", "no data in cursor");
            return;
        }

        Log.v("CURSOR YES DATA", "data found in cursor");
        showNametv = (TextView) findViewById(R.id.show_name_tv);
        epiNametv = (TextView) findViewById(R.id.epi_name_tv);
        seasontv = (TextView) findViewById(R.id.season_num_tv);
        epiNumtv = (TextView) findViewById(R.id.epi_num_tv);
        airDatetv = (TextView) findViewById(R.id.air_date_tv);
        networktv = (TextView) findViewById(R.id.network_tv);
        countrytv = (TextView) findViewById(R.id.country_code_tv);
        imgUrltv = (TextView) findViewById(R.id.img_url_tv);
        showNametv.setText(data.getString(COL_SHOW_NAME));
        epiNametv.setText(data.getString(COL_EPI_NAME));
        airDatetv.setText(data.getString(COL_AIR_DATE));
        seasontv.setText(data.getString(COL_SEASON));
        epiNumtv.setText(data.getString(COL_EPI_NUM));
        networktv.setText(data.getString(COL_NETWORK));
        countrytv.setText(data.getString(COL_COUNTRY_CODE));
        imgUrltv.setText(data.getString(COL_IMG_URL));
        Log.v("TEXTVIEW TEST", data.getString(COL_SHOW_NAME));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}