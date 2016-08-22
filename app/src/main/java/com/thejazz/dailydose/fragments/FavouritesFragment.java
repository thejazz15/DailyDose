package com.thejazz.dailydose.fragments;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.thejazz.dailydose.R;
import com.thejazz.dailydose.adapters.FavouritesAdapter;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.services.FetchFavouritesService;

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
    public void onStart() {
        super.onStart();
//        syncFavourites();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favs_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_sync){
            Toast.makeText(getActivity(), "Sync button clicked", Toast.LENGTH_SHORT).show();
            syncFavourites();
            getLoaderManager().restartLoader(TV_FAVS_LOADER_ID, null ,this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncFavourites(){
        Intent intent = new Intent(getActivity(), FetchFavouritesService.class);
        getActivity().startService(intent);
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
        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        favouritesAdapter = new FavouritesAdapter(getActivity(), null);
        recyclerView.setAdapter(favouritesAdapter);
        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        syncFavourites();
        Uri uri = TvShowsContract.FavsShowEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),
                uri,
                TV_SHOW_COLUMNS,
                null,
                null,
                TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE + " ASC"
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
