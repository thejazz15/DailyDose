package com.thejazz.dailydose;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.thejazz.dailydose.data.TvShowsContract.TvShowsEntry;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    public static SimpleCursorAdapter showsAdapter;
    public ListView showsListView;
    public ImageView showImageView;
    public static SwipeRefreshLayout swipeRefreshLayout;
    private String country;

    private static final int TV_SHOWS_LOADER_ID = 0;

    private static final String[] TV_SHOW_COLUMNS = {
            TvShowsEntry.TABLE_NAME + "." + TvShowsEntry._ID,
            TvShowsEntry.COLUMN_SHOW_NAME,
            TvShowsEntry.COLUMN_SEASON,
            TvShowsEntry.COLUMN_EPISODE_NUM,
            TvShowsEntry.COLUMN_AIR_DATE,
            TvShowsEntry.COLUMN_EPISODE_ID,
            TvShowsEntry.COLUMN_IMG_URL
    };

    public static final int COL_ID = 0;
    public static final int COL_SHOW_NAME = 1;
    public static final int COL_SEASON = 2;
    public static final int COL_EPISODE_NUM = 3;
    public static final int COL_AIR_DATE = 4;
    public static final int COL_EPISODE_ID = 5;
    public static final int COL_IMG_URL = 6;

    private void updateTvShowList() {
        FetchShowTask showTask = new FetchShowTask(getApplicationContext());
        String pref_country = Utility.getPrefferedCountry(getApplicationContext());
        showTask.execute(pref_country);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updateTvShowList();
    }


    public void onResume() {
        super.onResume();
        if (country != null && !country.equals(Utility.getPrefferedCountry(getApplicationContext()))) {
            getSupportLoaderManager().restartLoader(TV_SHOWS_LOADER_ID, null, this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportLoaderManager().initLoader(TV_SHOWS_LOADER_ID, null, this);
        showsListView = (ListView) findViewById(R.id.shows_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        showsAdapter = new SimpleCursorAdapter(
                getApplicationContext(),
                R.layout.list_item,
                null,
                new String[]{TvShowsEntry.COLUMN_SHOW_NAME,
                        TvShowsEntry.COLUMN_AIR_DATE,
                        TvShowsEntry.COLUMN_SEASON,
                        TvShowsEntry.COLUMN_EPISODE_NUM,
                        TvShowsEntry.COLUMN_IMG_URL},
                new int[]{R.id.show_name_tv, R.id.date_tv, R.id.season_details_tv, R.id.epi_details_tv, R.id.show_image_view},
                0
        );

        showsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex) {
                    case COL_IMG_URL:
                        showImageView = (ImageView) view;
                        String img_url = cursor.getString(COL_IMG_URL);
                        //Log.v("IMG_URL", img_url);
                        if (img_url != null && img_url != "")
                            new LoadImageTask(showImageView).execute(img_url);
                        return true;
                    case COL_AIR_DATE:
                        ((TextView) view).setText(Utility.formatAirDate(cursor.getString(COL_AIR_DATE)));
                        return true;
                }
                return false;
            }
        });
        setRefreshingtoFalse();
        showsListView.setAdapter(showsAdapter);

        showsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) adapterView.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getApplicationContext(), TvDetailActivity.class)
                            .putExtra("column_ID", cursor.getInt(COL_ID));
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_refresh) {
            swipeRefreshLayout.setRefreshing(true);
            updateTvShowList();
            setRefreshingtoFalse();
            return true;
        }
        if (id == R.id.action_location) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        updateTvShowList();
        setRefreshingtoFalse();
    }

    public static void setRefreshingtoFalse() {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        country = Utility.getPrefferedCountry(getApplicationContext());
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Uri tvShowsUri = TvShowsEntry.buildShowsWithCountryAndDate(country, date);
        Log.v("MainActivity", tvShowsUri.toString());
        return new CursorLoader(getApplicationContext(),
                tvShowsUri,
                TV_SHOW_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        showsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        showsAdapter.swapCursor(null);
    }
}
