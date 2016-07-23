package com.thejazz.dailydose;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.thejazz.dailydose.data.TvShowsContract.TvShowsEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * AsyncTask Class.
 */

public class FetchShowTask extends AsyncTask<String, Void, Void> {

    final String BASE_URI = "http://api.tvmaze.com/schedule?";
    final String DATE_PARAM = "date";
    final String COUNTRY_PARAM = "country";
    Context mContext;

    public FetchShowTask(Context context){
        mContext = context;
    }

    private void insertShowInfoJson(String TvJsonString)
            throws JSONException {
        JSONArray MainArray = new JSONArray(TvJsonString);
        Vector<ContentValues> vector = new Vector<ContentValues>(MainArray.length());
        String showName, airDate, img_url, episodeNum, season, showID, network, countryCode, episodeName, episodeID;
        for(int i = 0; i < MainArray.length(); i++){

            JSONObject tvShow = MainArray.getJSONObject(i);
            episodeID = Long.toString(tvShow.getLong("id"));
            airDate = tvShow.getString("airdate");
            season = Integer.toString(tvShow.getInt("season"));
            episodeNum = Utility.checkFieldIsNull(tvShow, "number");
            episodeName = tvShow.getString("name");
            JSONObject show_info = tvShow.getJSONObject("show");
            showName = show_info.getString("name");
            showID = show_info.getString("id");
            JSONObject networkObj = show_info.getJSONObject("network");
            network = networkObj.getString("name");
            JSONObject countryObj = networkObj.getJSONObject("country");
            countryCode = countryObj.getString("code");
            img_url = Utility.checkJSONObjectIsNull(show_info, "image");

            ContentValues values = new ContentValues();
            values.put(TvShowsEntry.COLUMN_SHOW_ID, showID);
            values.put(TvShowsEntry.COLUMN_SHOW_NAME, showName);
            values.put(TvShowsEntry.COLUMN_EPISODE_ID, episodeID);
            values.put(TvShowsEntry.COLUMN_EPISODE_NAME, episodeName);
            values.put(TvShowsEntry.COLUMN_AIR_DATE, airDate);
            values.put(TvShowsEntry.COLUMN_SEASON, season);
            values.put(TvShowsEntry.COLUMN_EPISODE_NUM, episodeNum);
            values.put(TvShowsEntry.COLUMN_COUNTRY_CODE, countryCode);
            values.put(TvShowsEntry.COLUMN_NETWORK, network);
            values.put(TvShowsEntry.COLUMN_IMG_URL, img_url);

            vector.add(values);
        }
        int inserted = 0;
        // add to database
        if ( vector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[vector.size()];
            vector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(TvShowsEntry.CONTENT_URI, cvArray);
        }
    }

    @Override
    protected Void doInBackground(String... param) {

        /**
         * Setting up http connection and fetching JSON data.
         */

        if(param.length == 0)
            return null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String TvJsonStr = null;

        try {
            Uri uri = Uri.parse(BASE_URI).buildUpon()
                    .appendQueryParameter(DATE_PARAM, new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                    .appendQueryParameter(COUNTRY_PARAM,param[0])
                    .build();

            URL url = new URL(uri.toString());


            // Create the request to API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                TvJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                TvJsonStr = null;
            }
            TvJsonStr = buffer.toString();
            try {
                Log.v("SHOW JSON STRING", TvJsonStr);
                insertShowInfoJson(TvJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            TvJsonStr = null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("MainActivity", "Error closing stream", e);
                }
            }
        }



        return null;
    }
/*
    @Override
    protected void onPostExecute(ArrayList<String> result) {
        if(result != null){
            MainActivity.setRefreshingtoFalse();
            MainActivity.showsAdapter.clear();
            for(String tvShow : result)
                MainActivity.showsAdapter.add(tvShow);
        }
    }*/
}






//new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(MY_URL_STRING);

    /*
    final String BASE_URI = "http://api.themoviedb.org/3/tv/on_the_air?";
    final String LANGUAGE_PARAM = "language";
    final String TIMEZONE_PARAM = "timezone";
    final String AIRDATE_GT_PARAM = "air_date.gte";
    final String API_KEY = "api_key";

    ------ TMDB API ------
    JSONObject TvMainObject = new JSONObject(TvJsonString);
    JSONArray TvShowsArray = TvMainObject.getJSONArray("results");
    String[] TvResults = new String[30];
    for(int i = 0; i < 30; i++){
        String name;
        JSONObject tvShow = TvShowsArray.getJSONObject(i);
        name = tvShow.getString("name");
        TvResults[i] = name;
    }
    */

// Construct the URL for the TMDB query
//Original URL - http://api.themoviedb.org/3/discover/tv?api_key=370bd9adf388af99c0fb4fd03ffc5f2c&air_date.gte=2016-07-13&language=en&timezone=IN
//URL url = new URL("http://api.themoviedb.org/3/tv/on_the_air?api_key=370bd9adf388af99c0fb4fd03ffc5f2c&language=en&timezone=IN");
            /*
            Uri uri = Uri.parse(BASE_URI).buildUpon()
                    .appendQueryParameter(LANGUAGE_PARAM, "en")
                    .appendQueryParameter(TIMEZONE_PARAM, "IN")
                    .appendQueryParameter(API_KEY, "370bd9adf388af99c0fb4fd03ffc5f2c")
                    //new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    .build();
            */
