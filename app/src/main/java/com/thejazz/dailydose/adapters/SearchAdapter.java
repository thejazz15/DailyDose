package com.thejazz.dailydose.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.stetho.common.Util;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.activites.SearchDetailActivity;
import com.thejazz.dailydose.utilities.Utility;
import com.thejazz.dailydose.utilities.VolleySingleton;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.UrlUtility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TheJazz on 04/08/16.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context mContext;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;
    private ContentValues[] cvArray;
    private RequestQueue requestQueue;
    private JsonObjectRequest episodeRequest;

    public SearchAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mContext = context;
        volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        imageLoader = volleySingleton.getImageLoader();
    }

    @Override
    public SearchAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.search_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    public void setTvShowList(ContentValues[] cvArray) {
        this.cvArray = new ContentValues[cvArray.length];
        this.cvArray = cvArray;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final SearchAdapter.MyViewHolder holder, int position) {
        final ContentValues values = cvArray[position];
        holder.showName.setText(values.get(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME).toString());
        String img_url = values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_MEDIUM);
        boolean isFav = Utility.isFavourite(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID));
        if(isFav)
            holder.imageButton.setImageResource(R.drawable.ic_favorite_white_48dp);
        if (img_url != null && !img_url.equals("")) {
            imageLoader.get(img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.image.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
        else
            holder.image.setImageResource(R.drawable.ic_info_black_24dp);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext.getApplicationContext(), SearchDetailActivity.class)
                        .putExtra("showName",values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (cvArray == null) ? 0 : cvArray.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView showName;
        ImageButton imageButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.show_image_view);
            showName = (TextView) itemView.findViewById(R.id.show_name_tv);
            imageButton = (ImageButton) itemView.findViewById(R.id.add_fav_btn);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentValues values = cvArray[getAdapterPosition()];
                    if(Utility.isFavourite(values)){
                        imageButton.setImageResource(R.drawable.ic_favorite_border_white_48dp);
                        int rowsDeleted = 0;
                        rowsDeleted = mContext.getContentResolver().delete(TvShowsContract.FavsShowEntry.CONTENT_URI,
                                TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID + " = ? ",
                                new String[]{values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID)});
                        if(rowsDeleted == 1)
                            Toast.makeText(mContext, "Removed " + values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME)+" from Favourites.",Toast.LENGTH_LONG).show();
                    }else {
                        imageButton.setImageResource(R.drawable.ic_favorite_white_48dp);
                        String episodeId = values.get(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID).toString();
                        if (!episodeId.equals("N/A"))
                            sendDetailEpisodeRequest(UrlUtility.buildEpisodeUrl(episodeId), values);
                        else {
                            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, "N/A");
                            values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, "N/A");
                            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, "N/A");
                            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, Long.MAX_VALUE);
                            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, "N/A");
                            values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, "N/A");
                            updateFavourites(values);
                        }
                    }
                }
            });
        }
    }

    private void sendDetailEpisodeRequest(String url, final ContentValues values) {
        Log.v("ENTERED REQUEST JSON", "Entered request function with url " + url);

        episodeRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("ENTERED DETAIL JSON", "Received response");
                ContentValues updatedValues = parseShowDetailsResponse(response, values);
                updateFavourites(updatedValues);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, "No Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(episodeRequest);
    }

    private ContentValues parseShowDetailsResponse(JSONObject object, ContentValues values) {
        Log.v("ENTERED DETAIL JSON", "Entered function");
        String airtime, season, epiNumber, epiName, summary, epi_id;
        long airdate;
        try {
            Log.v("FETCHING DATE DETAIL", "ENTERED TRY");
            epiName = object.getString("name");
            season = Integer.toString(object.getInt("season"));
            epiNumber = Integer.toString(object.getInt("number"));
            airdate = Utility.getMillisFromStringDate(object.getString("airdate"));
            Log.v("FETCHING DATE DETAIL", Utility.getDateFromMillis(airdate));
            airtime = object.getString("airtime");
            summary = object.getString("summary");
            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, epiName);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, season);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, epiNumber);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, airdate);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, airtime);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, summary);
        } catch (JSONException e) {
            Log.e("VOLLEY DETAILS ERROR", "Could not process details json.");
            e.printStackTrace();
        }
        return values;
    }

    private void updateFavourites(ContentValues values) {
        Log.v("UPDATE DETAIL", "Updating episode details.");
        String episodeId = values.get(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID).toString();
        Uri updated = mContext.getContentResolver().insert(TvShowsContract.FavsShowEntry.CONTENT_URI, values);
        Log.v("UPDATE DETAIL", "Updated episode details.");
        Toast.makeText(mContext,
                "Added " + values.get(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME).toString() + " to Favourites!",
                Toast.LENGTH_LONG).show();
    }

}
