package com.thejazz.dailydose.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.activites.PopularDetailActivity;
import com.thejazz.dailydose.utilities.VolleySingleton;

/**
 * Created by TheJazz on 10/08/16.
 */
public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context mContext;
    private ContentValues[] cvArray;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;

    public PopularAdapter(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        volleySingleton = VolleySingleton.getInstance();
        imageLoader = volleySingleton.getImageLoader();
    }

    public void setShowList(ContentValues[] cvArray) {
        this.cvArray = new ContentValues[cvArray.length];
        this.cvArray = cvArray;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.pop_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ContentValues values = cvArray[position];
        holder.showName.setText(values.getAsString("showName"));
        String img_url = values.getAsString("posterPath");
        if (img_url != null && img_url != "") {
            imageLoader.get(img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.image.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        } else
            holder.image.setImageResource(R.drawable.ic_info_black_24dp);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext.getApplicationContext(), PopularDetailActivity.class)
                        .putExtra("showName", values.getAsString("showName"));
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

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.show_image_view);
            showName = (TextView) itemView.findViewById(R.id.show_name_tv);

        }
    }
}
