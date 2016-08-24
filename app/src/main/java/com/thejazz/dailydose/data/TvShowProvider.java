package com.thejazz.dailydose.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.thejazz.dailydose.data.TvShowsContract.TvShowsEntry;
import com.thejazz.dailydose.data.TvShowsContract.FavsShowEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TheJazz on 20/07/16.
 */
public class TvShowProvider extends ContentProvider {

    public static final int SHOWS = 101;
    public static final int SHOW_WITH_EPISODE_ID = 103;
    public static final int SHOWS_WITH_COUNTRY_AND_DATE = 104;
    public static final int SHOWS_WITH_SHOW_ID = 105;
    public static final int SHOWS_ID = 106;
    public static final int FAVS = 107;
    public static final int FAVS_ID = 108;
    public static final int FAV_WITH_TODAY_DATE = 109;

    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private TvShowDbHelper myHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = TvShowsContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS,SHOWS);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/episode/id/*",SHOW_WITH_EPISODE_ID);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/*/*",SHOWS_WITH_COUNTRY_AND_DATE);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/*",SHOWS_ID);
        matcher.addURI(authority, TvShowsContract.FAV_SHOWS, FAVS);
        matcher.addURI(authority, TvShowsContract.FAV_SHOWS + "/show/*",SHOWS_WITH_SHOW_ID);
        matcher.addURI(authority, TvShowsContract.FAV_SHOWS + "/today/favs/*", FAV_WITH_TODAY_DATE);
        matcher.addURI(authority, TvShowsContract.FAV_SHOWS + "/*",FAVS_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        myHelper = new TvShowDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        SQLiteDatabase db = myHelper.getReadableDatabase();
        String country_code, date, show_id, episode_id;
        int match = sUriMatcher.match(uri);
        Log.v("QUERY URI MATCHED", "Matched uri is " + uri.toString());
        Log.v("QUERY INTEGER MATCHED", "Matched integer is " + Integer.toString(match));
        switch (match){
            case SHOW_WITH_EPISODE_ID:
                Log.v("QUERY from CP", "In matched EPISODE__ID");
                episode_id = TvShowsEntry.getEpisodeIdFromUri(uri);
                retCursor = db.query(TvShowsEntry.TABLE_NAME,
                        projection,
                        TvShowsEntry.COLUMN_EPISODE_ID + " = ? ",
                        new String[]{episode_id},
                        null,
                        null,
                        sortOrder);
                break;
            case SHOWS_WITH_SHOW_ID:
                show_id = FavsShowEntry.getShowIdFromUri(uri);
                retCursor = db.query(FavsShowEntry.TABLE_NAME,
                        projection,
                        FavsShowEntry.COLUMN_SHOW_ID + " = ? ",
                        new String[]{show_id},
                        null,
                        null,
                        sortOrder);
                break;
            case FAV_WITH_TODAY_DATE:
                String todayString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                Date todayDate = null;
                try {
                    todayDate = new SimpleDateFormat("yyyy-MM-dd").parse(todayString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long millisToday = todayDate.getTime();
                long millisTomorrow = millisToday + (1000 * 60 * 60 * 24);
                retCursor = db.query(FavsShowEntry.TABLE_NAME,
                        projection,
                        FavsShowEntry.COLUMN_AIR_DATE + " >= ? AND " + FavsShowEntry.COLUMN_AIR_DATE + " < ? ",
                        new String[]{Long.toString(millisToday),Long.toString(millisTomorrow)},
                        null,
                        null,
                        sortOrder);
                break;
            case SHOWS_WITH_COUNTRY_AND_DATE:
                Log.v("QUERY from CP", "In matched COUNTRY_AND_DATE");
                country_code = TvShowsEntry.getCountryCodeFromUri(uri);
                date = TvShowsEntry.getDateFromUri(uri);
                retCursor = db.query(TvShowsEntry.TABLE_NAME,
                        projection,
                        TvShowsEntry.COLUMN_COUNTRY_CODE + " = ? AND " + TvShowsEntry.COLUMN_AIR_DATE + " = ? ",
                        new String[]{country_code, date},
                        null,
                        null,
                        sortOrder);
                break;
            case SHOWS_ID:
                retCursor = db.query(TvShowsEntry.TABLE_NAME,
                        projection,
                        TvShowsEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVS_ID:
                retCursor = db.query(FavsShowEntry.TABLE_NAME,
                        projection,
                        FavsShowEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SHOWS:
                retCursor = db.query(TvShowsEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder);
                break;
            case FAVS:
                retCursor = db.query(FavsShowEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                Log.v("ContentProvider", "No uri matched!");
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case SHOWS_WITH_COUNTRY_AND_DATE:
                return TvShowsEntry.CONTENT_TYPE;
            case SHOWS:
                return TvShowsEntry.CONTENT_TYPE;
            case SHOWS_WITH_SHOW_ID:
                return FavsShowEntry.CONTENT_ITEM_TYPE;
            case FAVS:
                return FavsShowEntry.CONTENT_TYPE;
            case SHOWS_ID:
                return TvShowsEntry.CONTENT_ITEM_TYPE;
            case SHOW_WITH_EPISODE_ID:
                return TvShowsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = myHelper.getWritableDatabase();
        Uri retUri;
        long _id;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                _id = db.insert(TvShowsEntry.TABLE_NAME, null, contentValues);
                if(_id > 0)
                    retUri = TvShowsEntry.buildShowUri(_id);
                else
                    throw new android.database.SQLException("Could not insert row into " + uri);
                break;
            case FAVS:
                _id = db.insert(FavsShowEntry.TABLE_NAME, null, contentValues);
                if(_id > 0) {
                    Log.v("INSERTED ID", Long.toString(_id) + contentValues.getAsString(FavsShowEntry.COLUMN_SHOW_NAME));
                    retUri = FavsShowEntry.buildShowUri(_id);
                }
                else
                    throw new android.database.SQLException("Could not insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = myHelper.getWritableDatabase();
        int rowsDeleted;
        if ( null == selection ) selection = "1";
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                rowsDeleted = db.delete(TvShowsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVS:
                rowsDeleted = db.delete(FavsShowEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(selection == null || rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = myHelper.getWritableDatabase();
        int rowsUpdated;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                rowsUpdated = db.update(TvShowsEntry.TABLE_NAME, contentValues , selection, selectionArgs);
                break;
            case FAVS:
                rowsUpdated = db.update(FavsShowEntry.TABLE_NAME, contentValues , selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = myHelper.getWritableDatabase();
        long id;
        int returnCount = 0;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        id = db.insert(TvShowsEntry.TABLE_NAME, null, value);
                        if (id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case FAVS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        id = db.insert(FavsShowEntry.TABLE_NAME, null, value);
                        if (id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }


}
