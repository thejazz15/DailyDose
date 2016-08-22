package com.thejazz.dailydose.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.utilities.VolleySingleton;
import com.thejazz.dailydose.activites.PopularTvDetailActivity;
import com.thejazz.dailydose.fragments.FavouritesFragment;
import com.thejazz.dailydose.utilities.Utility;

/**
 * Created by TheJazz on 05/08/16.
 */
public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context mContext;
    private Cursor mCursor;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;

    public FavouritesAdapter(Context context, Cursor cursor){
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mCursor = cursor;
        volleySingleton = VolleySingleton.getInstance();
        imageLoader = volleySingleton.getImageLoader();
    }

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.favs_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.showName.setText(mCursor.getString(FavouritesFragment.COL_SHOW_NAME));
        holder.airDate.setText(Utility.formatAirDate(mCursor.getString(FavouritesFragment.COL_AIR_DATE)));
        Log.v("FavouritesAdapter",mCursor.getString(FavouritesFragment.COL_SHOW_NAME) + " , "+mCursor.getString(FavouritesFragment.COL_EPISODE_NAME));
        if(!mCursor.getString(FavouritesFragment.COL_EPISODE_NAME).equals("N/A")){
            holder.episodeName.setVisibility(View.VISIBLE);
            holder.episodeName.setText(mCursor.getString(FavouritesFragment.COL_EPISODE_NAME));
            holder.episodeTitle.setVisibility(View.VISIBLE);
            holder.seasonTitle.setVisibility(View.VISIBLE);
            holder.season.setVisibility(View.VISIBLE);
            holder.season.setText(Utility.formatNumber(mCursor.getString(FavouritesFragment.COL_SEASON)));
            holder.dash.setVisibility(View.VISIBLE);
            holder.episodeNum.setVisibility(View.VISIBLE);
            holder.episodeNum.setText(Utility.formatNumber(mCursor.getString(FavouritesFragment.COL_EPISODE_NUM)));
        }
        String img_url = mCursor.getString(FavouritesFragment.COL_IMG_URL);
        if (img_url != null && img_url != ""){
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
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView showName, airDate, episodeName, season, episodeNum, seasonTitle, episodeTitle, dash;
        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.show_image_view);
            showName = (TextView) itemView.findViewById(R.id.show_name_tv);
            airDate = (TextView) itemView.findViewById(R.id.date_tv);
            episodeName = (TextView) itemView.findViewById(R.id.episode_name_tv);
            season = (TextView) itemView.findViewById(R.id.season_details_tv);
            episodeNum = (TextView) itemView.findViewById(R.id.epi_details_tv);
            seasonTitle = (TextView) itemView.findViewById(R.id.season_title_tv);
            episodeTitle = (TextView) itemView.findViewById(R.id.epi_title_tv);
            dash = (TextView) itemView.findViewById(R.id.dash);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mCursor != null && mCursor.moveToPosition(getAdapterPosition())) {
                Intent intent = new Intent(mContext.getApplicationContext(), PopularTvDetailActivity.class)
                        .putExtra("_ID", mCursor.getInt(0));
//                Intent intent = new Intent(mContext.getApplicationContext(), ScrollingActivity.class);
                mContext.startActivity(intent);
            }
        }
    }
}
