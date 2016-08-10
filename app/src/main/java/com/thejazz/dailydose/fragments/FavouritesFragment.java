package com.thejazz.dailydose.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.adapters.FavouritesAdapter;
import com.thejazz.dailydose.adapters.TodayListAdapter;
import com.thejazz.dailydose.data.TvShowsContract;

public class FavouritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    private FavouritesAdapter favouritesAdapter;

    private static final int TV_FAVS_LOADER_ID = 0;

    private static final String[] TV_SHOW_COLUMNS = {
            TvShowsContract.FavsShowEntry.TABLE_NAME + "." + TvShowsContract.FavsShowEntry._ID,
            TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME,
            TvShowsContract.FavsShowEntry.COLUMN_IMG_URL,
            TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE,
            TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME,
            TvShowsContract.FavsShowEntry.COLUMN_SEASON,
            TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM
//            TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID,

    };

//    public static final int COL_EPISODE_ID = 6;
//    public static final int COL_IMG_URL = 7;

    public static final int COL_ID = 0;
    public static final int COL_SHOW_NAME = 1;
    public static final int COL_IMG_URL = 2;
    public static final int COL_AIR_DATE = 3;
    public static final int COL_EPISODE_NAME = 4;
    public static final int COL_SEASON = 5;
    public static final int COL_EPISODE_NUM = 6;

    public FavouritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(TV_FAVS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.fav_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        favouritesAdapter = new FavouritesAdapter(getActivity(), null);
        recyclerView.setAdapter(favouritesAdapter);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = TvShowsContract.FavsShowEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),
                uri,
                TV_SHOW_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        favouritesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        favouritesAdapter.swapCursor(null);
    }
}
