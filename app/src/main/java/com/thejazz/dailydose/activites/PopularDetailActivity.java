package com.thejazz.dailydose.activites;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.UrlUtility;
import com.thejazz.dailydose.utilities.Utility;
import com.thejazz.dailydose.utilities.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class PopularDetailActivity extends AppCompatActivity {

    public TextView showNametv, epiNametv, airDatetv, seasontv, epiNumtv, networktv, countrytv, imgUrltv, summarytv, imdbtv;
    public ImageView header;
    public FloatingActionButton fab;
    private CardView imdbCv, episodeDetailsCv, timeCv, summaryCv;
    private ProgressBar pBar;

    private ShareActionProvider mShareActionProvider;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private int _ID;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;
    private JsonObjectRequest objectRequest, detailRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_detail);
        toolbar = (Toolbar) findViewById(R.id.MyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.colorPrimary));
        volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getInstance().getRequestQueue();
        imageLoader = volleySingleton.getImageLoader();
        String showName = getIntent().getStringExtra("showName");
        String url = UrlUtility.buildSingleSearchUrl(showName);
        getShowDetails(url);
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

    private void getShowDetails(String url) {
        final ContentValues values = new ContentValues();
        objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String showId, showName, imgUrl, imgUrlOrg;
                    showId = Integer.toString(response.getInt("id"));
                    showName = response.getString("name");
                    imgUrl = Utility.checkJSONObjectIsNull(response, "image", "medium");
                    imgUrlOrg = Utility.checkJSONObjectIsNull(response, "image", "original");
                    JSONObject external_links = response.getJSONObject("externals");
                    String imdbId = external_links.getString("imdb");
                    String showSummary = Utility.formatSummary(response.getString("summary"));
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID, showId);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME, showName);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_MEDIUM, imgUrl);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_ORIGINAL, imgUrlOrg);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMDB_ID, imdbId);
                    values.put("showSummary", showSummary);
                    JSONObject linksObject = response.getJSONObject("_links");
                    String nextEpisodeUrl = Utility.checkIfShowHasNextEpisode(linksObject, "nextepisode");
                    if (nextEpisodeUrl.equals("N/A")) {
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, "N/A");
                        updateFavourite(values);
                    } else {
                        String parts[] = nextEpisodeUrl.split("/episodes/");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, parts[1]);
                        sendDetailEpisodeRequest(UrlUtility.buildEpisodeUrl(parts[1]), values);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(objectRequest);
    }

    public void sendDetailEpisodeRequest(String url, final ContentValues values) {
        detailRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseSyncResponse(response, values);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext().getApplicationContext(), "No Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(detailRequest);
    }


    private void parseSyncResponse(JSONObject object, ContentValues values) {
        String airtime, season, epiNumber, epiName, summary;
        long airdate;
        try {
            epiName = object.getString("name");
            season = Integer.toString(object.getInt("season"));
            epiNumber = Integer.toString(object.getInt("number"));
            airdate = Utility.getMillisFromStringDate(object.getString("airdate"));
            airtime = object.getString("airtime");
            summary = object.getString("summary");
            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, epiName);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, season);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, epiNumber);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, airdate);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, airtime);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, summary);
            updateFavourite(values);
        } catch (JSONException e) {
            Log.e("VOLLEY DETAILS ERROR", "Could not process details json.");
            e.printStackTrace();
        }
    }

    private void updateFavourite(final ContentValues values) {
        imdbCv = (CardView) findViewById(R.id.imdb_card_view);
        imdbCv.setVisibility(View.VISIBLE);
        episodeDetailsCv = (CardView) findViewById(R.id.episode_details_card_view);
        episodeDetailsCv.setVisibility(View.VISIBLE);
        timeCv = (CardView) findViewById(R.id.time_card_view);
        timeCv.setVisibility(View.VISIBLE);
        summaryCv = (CardView) findViewById(R.id.summary_card_view);
        summaryCv.setVisibility(View.VISIBLE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        epiNametv = (TextView) findViewById(R.id.episode_name_tv);
        seasontv = (TextView) findViewById(R.id.season_details_tv);
        epiNumtv = (TextView) findViewById(R.id.episode_details_tv);
        airDatetv = (TextView) findViewById(R.id.time_tv);
        summarytv = (TextView) findViewById(R.id.summary_tv);
        imdbtv = (TextView) findViewById(R.id.imdb_tv);
        header = (ImageView) findViewById(R.id.bgheader);
        pBar = (ProgressBar) findViewById(R.id.popular_progress);

        if(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE).equals("N/A"))
                airDatetv.setText(R.string.next_episode_tba);
        else
            airDatetv.setText(Utility.getDateFromMillis(values.getAsLong(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE)));
        if(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SEASON).equals("N/A"))
            episodeDetailsCv.setVisibility(View.GONE);
        boolean isFav = Utility.isFavourite(values);
        if (isFav)
            fab.setImageResource(R.drawable.ic_favorite_white_48dp);
        collapsingToolbarLayout.setTitle(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
        epiNametv.setText(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME));
        seasontv.setText(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SEASON));
        epiNumtv.setText(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM));
        if (values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY).equals("")
                || values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY).equals("N/A"))
            summarytv.setText(values.getAsString("showSummary"));
        else
            summarytv.setText(Utility.formatSummary(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY)));
        imdbtv.setText(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME) + " IMDb Page");
        String img_url = values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_ORIGINAL);
        if (img_url != null && img_url != "") {
            imageLoader.get(img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    header.setImageBitmap(response.getBitmap());
                    pBar.setVisibility(View.GONE);
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
                        .putExtra("imdbId", values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_IMDB_ID));
                startActivity(intent);
            }
        });
    }


}
