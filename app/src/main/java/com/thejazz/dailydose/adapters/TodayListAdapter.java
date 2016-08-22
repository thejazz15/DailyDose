package com.thejazz.dailydose.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
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
import com.thejazz.dailydose.activites.TvDetailActivity;
import com.thejazz.dailydose.fragments.TodayFragment;
import com.thejazz.dailydose.utilities.Utility;
import com.thejazz.dailydose.utilities.VolleySingleton;

/**
 * Created by TheJazz on 03/08/16.
 */
public class TodayListAdapter extends RecyclerView.Adapter<TodayListAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Cursor mCursor;
    private Context mContext;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;

    public TodayListAdapter(Context context, Cursor cursor){
        inflater = LayoutInflater.from(context);
        mContext = context;
        mCursor = cursor;
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
        View view = inflater.inflate(R.layout.list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        Log.v("CURSOR DATA "+position, mCursor.getString(1));
        holder.showName.setText(mCursor.getString(TodayFragment.COL_SHOW_NAME));
        holder.season.setText(Utility.formatNumber(mCursor.getString(TodayFragment.COL_SEASON)));
        if(mCursor.getString(TodayFragment.COL_EPISODE_NUM).equals("")){
            holder.episodeTitle.setVisibility(View.GONE);
            holder.episode.setVisibility(View.GONE);
        }
        else
            holder.episode.setText(Utility.formatNumber(mCursor.getString(TodayFragment.COL_EPISODE_NUM)));
        holder.date.setText(Utility.formatAirDate(mCursor.getString(TodayFragment.COL_AIR_DATE)));
        String img_url = mCursor.getString(TodayFragment.COL_IMG_URL);
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

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView image;
        TextView showName, season, episode, date, episodeTitle;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.show_image_view);
            showName = (TextView) itemView.findViewById(R.id.show_name_tv);
            season = (TextView) itemView.findViewById(R.id.season_details_tv);
            episode = (TextView) itemView.findViewById(R.id.epi_details_tv);
            date = (TextView) itemView.findViewById(R.id.date_tv);
            episodeTitle = (TextView) itemView.findViewById(R.id.epi_title_tv);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mCursor != null && mCursor.moveToPosition(getAdapterPosition())) {
                Intent intent = new Intent(mContext.getApplicationContext(), TvDetailActivity.class)
                        .putExtra("column_ID", mCursor.getInt(0));
                mContext.startActivity(intent);
            }
        }
    }
}
