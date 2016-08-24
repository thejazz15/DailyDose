package com.thejazz.dailydose.activites;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SearchDetailActivity extends AppCompatActivity {

    public TextView networktv, summarytv, imdbtv;
    public ImageView header;
    public FloatingActionButton fab;
    private CardView imdbCv, timeCv, summaryCv;
    private ProgressBar pBar;

    private ShareActionProvider mShareActionProvider;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;
    private JsonObjectRequest objectRequest, episodeRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_detail);
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
                    String showId, showName, imgUrlOrg;
                    showId = Integer.toString(response.getInt("id"));
                    showName = response.getString("name");
                    imgUrlOrg = Utility.checkJSONObjectIsNull(response, "image", "original");
                    JSONObject external_links = response.getJSONObject("externals");
                    String imdbId = external_links.getString("imdb");
                    JSONObject network = response.getJSONObject("network");
                    String networkName = network.getString("name");
                    JSONObject countryObj = network.getJSONObject("country");
                    String countryCode = countryObj.getString("code");
                    String summary = response.getString("summary");

                    values.put("showId", showId);
                    values.put("showName", showName);
                    values.put("imgOriginal", imgUrlOrg);
                    values.put("imdbId", imdbId);
                    values.put("networkName", networkName);
                    values.put("countryCode", countryCode);
                    values.put("summary", summary);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMDB_ID, imdbId);
                    showSearchItemDetails(values);
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


    private void showSearchItemDetails(final ContentValues values) {
        imdbCv = (CardView) findViewById(R.id.imdb_card_view);
        imdbCv.setVisibility(View.VISIBLE);
        timeCv = (CardView) findViewById(R.id.time_card_view);
        timeCv.setVisibility(View.VISIBLE);
        summaryCv = (CardView) findViewById(R.id.summary_card_view);
        summaryCv.setVisibility(View.VISIBLE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        summarytv = (TextView) findViewById(R.id.summary_tv);
        imdbtv = (TextView) findViewById(R.id.imdb_tv);
        header = (ImageView) findViewById(R.id.bgheader);
        networktv = (TextView) findViewById(R.id.network_tv);
        collapsingToolbarLayout.setTitle(values.getAsString("showName"));
        pBar = (ProgressBar) findViewById(R.id.search_progress);

        boolean isFav = Utility.isFavourite(values.getAsString("showId"));
        if(isFav)
            fab.setImageResource(R.drawable.ic_favorite_white_48dp);



        networktv.setText(getString(R.string.network_and_country_code, values.getAsString("networkName"), values.getAsString("countryCode")));
        if (values.getAsString("summary").equals(""))
            summarytv.setText("Currently not available.");
        else
            summarytv.setText(Utility.formatSummary(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY)));
        imdbtv.setText(getString(R.string.imdb_string, values.getAsString("showName")));
        String img_url = values.getAsString("imgOriginal");
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
                        .putExtra("imdbId", values.getAsString("imdbId"));
                startActivity(intent);
            }
        });
    }


}
