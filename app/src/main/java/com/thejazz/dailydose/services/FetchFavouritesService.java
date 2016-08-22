package com.thejazz.dailydose.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.thejazz.dailydose.utilities.VolleySingleton;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.UrlUtility;
import com.thejazz.dailydose.utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TheJazz on 20/08/16.
 */
public class FetchFavouritesService extends IntentService {

    private RequestQueue requestQueue;
    private JsonObjectRequest objectRequest, detailRequest;
    private String LOG_TAG = "FetchFavouritesService";

    public FetchFavouritesService() {
        super("FetchFavouritesService");
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        Log.v(LOG_TAG, "Started service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Cursor cursor = getContentResolver().query(TvShowsContract.FavsShowEntry.CONTENT_URI,
                new String[]{TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME},
                null,
                null,
                null);
        Log.v(LOG_TAG, "Cursor length = "+cursor.getCount());
        String showName;
        if(cursor.moveToFirst()) {
            do {
                showName = cursor.getString(cursor.getColumnIndex(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
                Log.v(LOG_TAG, UrlUtility.buildSingleSearchUrl(showName));
                sendSyncRequest(UrlUtility.buildSingleSearchUrl(showName));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void sendSyncRequest(String url){
        objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ContentValues values = new ContentValues();
                    String showId, showName, imgUrl;
                    showId = Integer.toString(response.getInt("id"));
                    showName = response.getString("name");
                    JSONObject imageObject = response.getJSONObject("image");
                    imgUrl = imageObject.getString("medium");
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID, showId);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME, showName);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL, imgUrl);
                    JSONObject linksObject = response.getJSONObject("_links");
                    String nextEpisodeUrl = Utility.checkIfShowHasNextEpisode(linksObject, "nextepisode");
                    if(nextEpisodeUrl.equals("N/A")) {
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, "N/A");
                        Log.v(LOG_TAG, "No Next Episode");
                        updateFavourite(values);
                    }else{
                        String parts[] = nextEpisodeUrl.split("/episodes/");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, parts[1]);
                        Log.v(LOG_TAG, "Next Episode "+parts[1]);
                        sendDetailEpisodeRequest(UrlUtility.buildEpisodeUrl(parts[1]),values);
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
        Log.v(LOG_TAG, "Episode URL " + url);

        detailRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(LOG_TAG, "Received response");
                parseSyncResponse(response, values);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(detailRequest);
    }



    private void parseSyncResponse(JSONObject object, ContentValues values){
        String airdate = "N/A", airtime, season, epiNumber, epiName, summary;
        try {
            epiName = object.getString("name");
            season = Integer.toString(object.getInt("season"));
            epiNumber = Integer.toString(object.getInt("number"));
            airdate = object.getString("airdate");
            Log.v(LOG_TAG, airdate);
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

    private void updateFavourite(ContentValues values){
        Log.v(LOG_TAG, "Updating.. with show name = "+values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
        Uri uri;
            uri = getApplicationContext().getContentResolver().insert(TvShowsContract.FavsShowEntry.CONTENT_URI, values);
        if (uri != null) {
            Log.v(LOG_TAG, "Return uri = "+uri.toString() + " - Favourites Updated Successfully!");
        }
    }

}
