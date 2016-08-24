package com.thejazz.dailydose.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.thejazz.dailydose.DividerItemDecoration;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.activites.MainActivity;
import com.thejazz.dailydose.utilities.VolleySingleton;
import com.thejazz.dailydose.activites.SettingsActivity;
import com.thejazz.dailydose.adapters.PopularAdapter;
import com.thejazz.dailydose.utilities.UrlUtility;
import com.thejazz.dailydose.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by TheJazz on 02/08/16.
 */
public class PopularFragment extends Fragment {

    private RecyclerView recyclerView;
    private PopularAdapter myAdapter;
    private RequestQueue requestQueue;
    private JsonObjectRequest jsonObjectRequest;
    private ContentValues[] cvArray;
    private ProgressBar pBar;
    private TextView noNetTv;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_location) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.popular_layout, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.pop_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        myAdapter = new PopularAdapter(getActivity());
        recyclerView.setAdapter(myAdapter);
        pBar = (ProgressBar) view.findViewById(R.id.popular_progress);
        noNetTv = (TextView) view.findViewById(R.id.no_internet_tv);
        sendJsonRequest();
        return view;
    }

    private void sendJsonRequest(){
        String url = UrlUtility.buildPopularUrl();
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pBar.setVisibility(View.GONE);
                cvArray = parseJsonResponse(response);
                myAdapter.setShowList(cvArray);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pBar.setVisibility(View.GONE);
                noNetTv.setVisibility(View.VISIBLE);
                String failString = Utility.VolleyErrorMessage(error);
                noNetTv.setText(failString);
                Toast.makeText(getActivity(),"Could not load Popular TV Shows due to No Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private ContentValues[] parseJsonResponse(JSONObject response){
        ContentValues cvArray[] = null;
        try {
            JSONArray resultsArray = response.getJSONArray("results");
            Vector<ContentValues> vector = new Vector<ContentValues>(resultsArray.length());
            for(int i = 0; i< resultsArray.length(); i++){
                JSONObject show = resultsArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                String posterPath = UrlUtility.buildPosterPath(show.getString("poster_path"));
                String showName = show.getString("name");
                values.put("posterPath",posterPath);
                values.put("showName", showName);
                vector.add(values);
            }
            cvArray = new ContentValues[vector.size()];
            vector.toArray(cvArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cvArray;
    }
}
