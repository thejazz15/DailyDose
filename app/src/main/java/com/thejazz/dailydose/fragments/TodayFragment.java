package com.thejazz.dailydose.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.VolleySingleton;
import com.thejazz.dailydose.adapters.TodayListAdapter;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.UrlUtility;
import com.thejazz.dailydose.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by TheJazz on 02/08/16.
 */
public class TodayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener  {

    private RequestQueue requestQueue;
    private JsonArrayRequest request;
    private String url;
    private RecyclerView recyclerView;
    public static SwipeRefreshLayout swipeRefreshLayout;
    private TodayListAdapter myAdapter;
    private String pref_country;
    //private ProgressBar progressBar;


    private static final int TV_TODAY_LOADER_ID = 0;

    private static final String[] TV_SHOW_COLUMNS = {
            TvShowsContract.TvShowsEntry.TABLE_NAME + "." + TvShowsContract.TvShowsEntry._ID,
            TvShowsContract.TvShowsEntry.COLUMN_SHOW_NAME,
            TvShowsContract.TvShowsEntry.COLUMN_SEASON,
            TvShowsContract.TvShowsEntry.COLUMN_EPISODE_NUM,
            TvShowsContract.TvShowsEntry.COLUMN_AIR_DATE,
            TvShowsContract.TvShowsEntry.COLUMN_EPISODE_ID,
            TvShowsContract.TvShowsEntry.COLUMN_IMG_URL
    };

    public static final int COL_ID = 0;
    public static final int COL_SHOW_NAME = 1;
    public static final int COL_SEASON = 2;
    public static final int COL_EPISODE_NUM = 3;
    public static final int COL_AIR_DATE = 4;
    public static final int COL_EPISODE_ID = 5;
    public static final int COL_IMG_URL = 6;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        //sendJsonRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pref_country != null && !pref_country.equals(Utility.getPrefferedCountry(getActivity()))) {
            getLoaderManager().restartLoader(TV_TODAY_LOADER_ID, null, this);
            //progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.today_layout, container, false);
        //progressBar = (ProgressBar) view.findViewById(R.id.search_progress);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.today_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myAdapter = new TodayListAdapter(getActivity(), null);
        recyclerView.setAdapter(myAdapter);
        //progressBar.setVisibility(View.VISIBLE);
        sendJsonRequest();
        return view;
    }

    private void setRefreshingtoFalse() {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

//    private void setLoadingtoFalse(){
//        if(progressBar.getVisibility() == View.VISIBLE)
//            progressBar.setVisibility(View.GONE);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TV_TODAY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void sendJsonRequest() {

        pref_country = Utility.getPrefferedCountry(getActivity());
        url = UrlUtility.buildTodayUrl(pref_country);
        request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                parseJsonResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getActivity(), "ERROR" + error, Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }

    private void parseJsonResponse(JSONArray array) {
        String showName, airDate, img_url, episodeNum, season, showID, network, countryCode, episodeName, episodeID;
        Vector<ContentValues> vector = new Vector<ContentValues>(array.length());
        if (array.length() == 0 || array == null)
            return;
        try {
            for (int i = 0; i < array.length(); i++) {

                JSONObject tvShow = array.getJSONObject(i);
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
                values.put(TvShowsContract.TvShowsEntry.COLUMN_SHOW_ID, showID);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_SHOW_NAME, showName);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_EPISODE_ID, episodeID);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_EPISODE_NAME, episodeName);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_AIR_DATE, airDate);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_SEASON, season);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_EPISODE_NUM, episodeNum);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_COUNTRY_CODE, countryCode);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_NETWORK, network);
                values.put(TvShowsContract.TvShowsEntry.COLUMN_IMG_URL, img_url);

                vector.add(values);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        int inserted = 0;
        // add to database
        if (vector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[vector.size()];
            vector.toArray(cvArray);
            inserted = getActivity().getContentResolver().bulkInsert(TvShowsContract.TvShowsEntry.CONTENT_URI, cvArray);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Uri tvShowsUri = TvShowsContract.TvShowsEntry.buildShowsWithCountryAndDate(Utility.getPrefferedCountry(getActivity()), date);
        Log.v("MainActivity", tvShowsUri.toString());
        return new CursorLoader(getActivity(),
                tvShowsUri,
                TV_SHOW_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        myAdapter.swapCursor(data);
        //setLoadingtoFalse();
        setRefreshingtoFalse();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        myAdapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        sendJsonRequest();
    }
}
