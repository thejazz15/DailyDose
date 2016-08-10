package com.thejazz.dailydose.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.thejazz.dailydose.data.TvShowsContract.TvShowsEntry;
import com.thejazz.dailydose.data.TvShowsContract.FavsShowEntry;

/**
 * Created by TheJazz on 19/07/16.
 */
public class TvShowDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "tvshows.db";

    public TvShowDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TABLE = "CREATE TABLE "+ TvShowsEntry.TABLE_NAME + " ("
                + TvShowsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +TvShowsEntry.COLUMN_SHOW_ID + " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_SHOW_NAME+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_AIR_DATE+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_SEASON+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_EPISODE_NUM+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_EPISODE_ID+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_EPISODE_NAME+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_COUNTRY_CODE+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_NETWORK+ " TEXT NOT NULL, "
                +TvShowsEntry.COLUMN_IMG_URL+ " TEXT, "
                + " UNIQUE (" + TvShowsEntry.COLUMN_EPISODE_ID +") ON CONFLICT REPLACE);";

        final String SQL_CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FavsShowEntry.TABLE_NAME + " ("
                + FavsShowEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +FavsShowEntry.COLUMN_SHOW_ID + " TEXT NOT NULL, "
                +FavsShowEntry.COLUMN_SHOW_NAME + " TEXT NOT NULL, "
                +FavsShowEntry.COLUMN_AIR_DATE + " TEXT, "
                +FavsShowEntry.COLUMN_AIR_TIME + " TEXT, "
                +FavsShowEntry.COLUMN_SEASON+ " TEXT, "
                +FavsShowEntry.COLUMN_EPISODE_NUM+ " TEXT, "
                +FavsShowEntry.COLUMN_EPISODE_NAME+ " TEXT, "
                +FavsShowEntry.COLUMN_EPISODE_ID+ " TEXT, "
                +FavsShowEntry.COLUMN_SUMMARY+ " TEXT, "
                +FavsShowEntry.COLUMN_IMG_URL+ " TEXT, "
                + " UNIQUE (" + FavsShowEntry.COLUMN_SHOW_ID +") ON CONFLICT REPLACE);";

        Log.v("QUERY CREATED", "Query string created");

        try {
            sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
            sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITES_TABLE);
            Log.v("DB CREATED", SQL_CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("DB ERROR","");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TvShowsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavsShowEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
