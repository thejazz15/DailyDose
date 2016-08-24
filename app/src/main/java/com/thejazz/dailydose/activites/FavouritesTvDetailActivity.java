package com.thejazz.dailydose.activites;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.VolleySingleton;
import com.thejazz.dailydose.data.TvShowsContract.FavsShowEntry;
import com.thejazz.dailydose.utilities.Utility;


public class FavouritesTvDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public TextView showNametv, epiNametv, airDatetv, seasontv, epiNumtv, networktv, countrytv, imgUrltv, summarytv, imdbtv, timeNetwork;
    public ImageView header;
    private CardView imdbCv, episodeDetailsCv;
    private FloatingActionButton fab;

    private ShareActionProvider mShareActionProvider;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private int _ID;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;

    private static final int DETAIL_LOADER_ID = 0;

    private static final String[] TV_SHOW_COLUMNS = {
            FavsShowEntry._ID,
            FavsShowEntry.COLUMN_SHOW_NAME,
            FavsShowEntry.COLUMN_EPISODE_NAME,
            FavsShowEntry.COLUMN_SEASON,
            FavsShowEntry.COLUMN_EPISODE_NUM,
            FavsShowEntry.COLUMN_AIR_DATE,
            FavsShowEntry.COLUMN_IMG_URL_ORIGINAL,
            FavsShowEntry.COLUMN_SUMMARY,
            FavsShowEntry.COLUMN_IMDB_ID,
            FavsShowEntry.COLUMN_SHOW_ID,
            FavsShowEntry.COLUMN_SHOW_SUMMARY,
            FavsShowEntry.COLUMN_AIR_TIME
    };

    private static final int COL_ID = 0;
    private static final int COL_SHOW_NAME = 1;
    private static final int COL_EPI_NAME = 2;
    private static final int COL_SEASON = 3;
    private static final int COL_EPI_NUM = 4;
    private static final int COL_AIR_DATE = 5;
    private static final int COL_IMG_URL = 6;
    private static final int COL_SUMMARY = 7;
    private static final int COL_IMDB_ID = 8;
    private static final int COL_SHOW_ID = 9;
    private static final int COL_SHOW_SUMMARY = 10;
    private static final int COL_AIR_TIME = 11;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fav_tv_detail_layout);
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        toolbar = (Toolbar) findViewById(R.id.MyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.colorPrimary));
        volleySingleton = VolleySingleton.getInstance();
        imageLoader = volleySingleton.getImageLoader();
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
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = this.getIntent();

        if (intent != null && intent.hasExtra("_ID")) {
            _ID = intent.getIntExtra("_ID", 0);
            Log.v("_ID", Integer.toString(_ID));
        }
        Uri episodeUri = FavsShowEntry.buildShowUri(_ID);
        Log.v("EPISODE_URI", episodeUri.toString());
        return new CursorLoader(getApplicationContext(),
                episodeUri,
                TV_SHOW_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        Log.v("CURSOR TEST", "Checking if data is found.");
        if (!data.moveToFirst()) {
            Log.v("CURSOR NO DATA", "no data in cursor");
            return;
        }

        Log.v("CURSOR YES DATA", "data found in cursor");

        imdbCv = (CardView) findViewById(R.id.imdb_card_view);
        episodeDetailsCv = (CardView) findViewById(R.id.episode_details_card_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        epiNametv = (TextView) findViewById(R.id.episode_name_tv);
        seasontv = (TextView) findViewById(R.id.season_details_tv);
        epiNumtv = (TextView) findViewById(R.id.episode_details_tv);
        airDatetv = (TextView) findViewById(R.id.time_tv);
        summarytv = (TextView) findViewById(R.id.summary_tv);
        imdbtv = (TextView) findViewById(R.id.imdb_tv);
        header = (ImageView) findViewById(R.id.bgheader);
        timeNetwork = (TextView) findViewById(R.id.time_network_tv);

        boolean isFav = Utility.isFavourite(data.getString(COL_SHOW_ID));
        if (isFav)
            fab.setImageResource(R.drawable.ic_favorite_white_48dp);

        collapsingToolbarLayout.setTitle(data.getString(COL_SHOW_NAME));

        epiNametv.setText(data.getString(COL_EPI_NAME));
        if (data.getLong(COL_AIR_DATE) == Long.MAX_VALUE)
            airDatetv.setText(R.string.next_episode_tba);
        else
            airDatetv.setText(Utility.getDateFromMillis(data.getLong(COL_AIR_DATE)));
        if(data.getString(COL_AIR_TIME).equals("N/A"))
            timeNetwork.setVisibility(View.GONE);
        else
            timeNetwork.setText(getString(R.string.time_network, data.getString(COL_AIR_TIME)));
        seasontv.setText(data.getString(COL_SEASON));
        epiNumtv.setText(data.getString(COL_EPI_NUM));
        if (data.getString(COL_SUMMARY).equals("")
                || data.getString(COL_SUMMARY).equals("N/A"))
            summarytv.setText(Utility.formatSummary(data.getString(COL_SHOW_SUMMARY)));
        else
            summarytv.setText(Utility.formatSummary(data.getString(COL_SUMMARY)));
        if (data.getString(COL_SEASON).equals("N/A"))
            episodeDetailsCv.setVisibility(View.GONE);
        imdbtv.setText(data.getString(COL_SHOW_NAME) + " IMDb Page");
        String img_url = data.getString(COL_IMG_URL);
        if (img_url != null && img_url != "") {
            imageLoader.get(img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    header.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        } else
            header.setImageResource(R.drawable.ic_info_black_24dp);
        imdbCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), IMDbActivity.class)
                        .putExtra("imdbId", data.getString(COL_IMDB_ID));
                startActivity(intent);
            }
        });
        Log.v("TEXTVIEW TEST", data.getString(COL_SHOW_NAME));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}