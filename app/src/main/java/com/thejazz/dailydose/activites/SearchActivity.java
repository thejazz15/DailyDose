package com.thejazz.dailydose.activites;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.thejazz.dailydose.DividerItemDecoration;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.utilities.VolleySingleton;
import com.thejazz.dailydose.adapters.SearchAdapter;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.UrlUtility;
import com.thejazz.dailydose.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class SearchActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private EditText searchEt;
    private Button searchBtn;
    private ProgressBar pBar;
    private TextView NoResultsTv;
    private RecyclerView recyclerView;
    private JsonArrayRequest arrayRequest;
    private String url;
    private SearchAdapter myAdapter;
    private ContentValues[] cvArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        recyclerView = (RecyclerView) findViewById(R.id.search_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        myAdapter = new SearchAdapter(getApplicationContext());
        recyclerView.setAdapter(myAdapter);
        searchEt = (EditText) findViewById(R.id.search_et);
        searchBtn = (Button) findViewById(R.id.search_btn);
        NoResultsTv = (TextView) findViewById(R.id.no_results_tv);
        pBar = (ProgressBar) findViewById(R.id.search_progress);
        pBar.setVisibility(View.GONE);
        NoResultsTv.setVisibility(View.GONE);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchEt.getText() != null || searchEt.getText().toString() != "" || searchEt.getText().length() > 0) {
                    pBar.setVisibility(View.VISIBLE);
                    NoResultsTv.setVisibility(View.GONE);
                    sendJsonRequest(searchEt.getText().toString());
                }
            }
        });
    }

    private void sendJsonRequest(String searchQuery) {
        url = UrlUtility.buildSearchUrl(searchQuery);
        arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cvArray = parseJsonResponse(response);
                pBar.setVisibility(View.GONE);
                if (cvArray.length == 0)
                    NoResultsTv.setVisibility(View.VISIBLE);
                myAdapter.setTvShowList(cvArray);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(arrayRequest);
    }

    private ContentValues[] parseJsonResponse(JSONArray array) {
        ContentValues[] cvArray = new ContentValues[array.length()];
        Vector<ContentValues> vector = new Vector<ContentValues>(array.length());
        String showName, imgUrl, id;

        if (array.length() > 0 && array != null) {
            try {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject mainObject = array.getJSONObject(i);
                    JSONObject showInfo = mainObject.getJSONObject("show");
                    showName = showInfo.getString("name");
                    id = Integer.toString(showInfo.getInt("id"));
                    imgUrl = Utility.checkJSONObjectIsNull(showInfo, "image");
                    JSONObject linksObject = showInfo.getJSONObject("_links");
                    String nextEpisodeUrl = Utility.checkIfShowHasNextEpisode(linksObject, "nextepisode");
                    Log.v("NEXT EPISODE", nextEpisodeUrl + " " + showName);

                    ContentValues values = new ContentValues();

                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID, id);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME, showName);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL, imgUrl);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, "N/A");

                    if(nextEpisodeUrl.equals("N/A")) {
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, "N/A");
                        Log.v("NEXT AIRDATE IN SEARCH", "No url found");
                    }else{
                        String parts[] = nextEpisodeUrl.split("/episodes/");
                        Log.v("NEXT AIRDATE IN SEARCH", "Url found with episode id "+ parts[1]);
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, parts[1]);
                  }
                    vector.add(values);
                }
                cvArray = new ContentValues[vector.size()];
                vector.toArray(cvArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cvArray;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu);
        return true;
    }
}
