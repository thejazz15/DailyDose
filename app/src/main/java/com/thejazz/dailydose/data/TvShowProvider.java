package com.thejazz.dailydose.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.thejazz.dailydose.data.TvShowsContract.TvShowsEntry;
/**
 * Created by TheJazz on 20/07/16.
 */
public class TvShowProvider extends ContentProvider {

    public static final int SHOWS = 101;
    public static final int SHOWS_WITH_COUNTRY = 102;
    public static final int SHOWS_WITH_COUNTRY_AND_DATE = 103;
    public static final int SHOWS_WITH_SHOW_ID = 104;
    public static final int SHOWS_ID = 105;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    public static TvShowDbHelper myHelper;

    private static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = TvShowsContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS,SHOWS);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/*",SHOWS_WITH_COUNTRY);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/*/*",SHOWS_WITH_COUNTRY_AND_DATE);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/#",SHOWS_WITH_SHOW_ID);
        matcher.addURI(authority, TvShowsContract.PATH_SHOWS + "/id/#",SHOWS_ID);
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
        String country_code, date, show_id;
        switch (sUriMatcher.match(uri)){
            case SHOWS_WITH_COUNTRY_AND_DATE:
                country_code = TvShowsEntry.getCountryCodeFromUri(uri);
                date = TvShowsEntry.getDateFromUri(uri);
                retCursor = myHelper.getReadableDatabase()
                        .query(TvShowsEntry.TABLE_NAME,
                                projection,
                                TvShowsEntry.COLUMN_COUNTRY_CODE + " = ? AND " + TvShowsEntry.COLUMN_AIR_DATE + " = ? ",
                                new String[]{country_code, date},
                                null,
                                null,
                                sortOrder);
                break;
            case SHOWS_WITH_COUNTRY:
                country_code = TvShowsEntry.getCountryCodeFromUri(uri);
                retCursor = myHelper.getReadableDatabase()
                        .query(TvShowsEntry.TABLE_NAME,
                                projection,
                                TvShowsEntry.COLUMN_COUNTRY_CODE + " = ? ",
                                new String[]{country_code},
                                null,
                                null,
                                sortOrder);
                break;
            case SHOWS:
                retCursor = myHelper.getReadableDatabase()
                        .query(TvShowsEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder);
                break;
            case SHOWS_WITH_SHOW_ID:
                show_id = TvShowsEntry.getShowIdFromUri(uri);
                retCursor = myHelper.getReadableDatabase()
                        .query(TvShowsEntry.TABLE_NAME,
                                projection,
                                TvShowsEntry.COLUMN_SHOW_ID + " = ? ",
                                new String[]{show_id},
                                null,
                                null,
                                sortOrder);
                break;
            case SHOWS_ID:
                retCursor = myHelper.getReadableDatabase()
                        .query(TvShowsEntry.TABLE_NAME,
                                projection,
                                TvShowsEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                                selectionArgs,
                                null,
                                null,
                                sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case SHOWS_WITH_COUNTRY_AND_DATE:
                return TvShowsEntry.CONTENT_ITEM_TYPE;
            case SHOWS_WITH_COUNTRY:
                return TvShowsEntry.CONTENT_TYPE;
            case SHOWS:
                return TvShowsEntry.CONTENT_TYPE;
            case SHOWS_WITH_SHOW_ID:
                return TvShowsEntry.CONTENT_ITEM_TYPE;
            case SHOWS_ID:
                return TvShowsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = myHelper.getWritableDatabase();
        Uri retUri;
        long _id;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                _id = db.insert(TvShowsEntry.TABLE_NAME, null, contentValues);
                if(_id > 0)
                    retUri = TvShowsEntry.buildShowUri(_id);
                else
                    throw new android.database.SQLException("Could not insert row into" + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = myHelper.getWritableDatabase();
        long _id;
        int rowsDeleted;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                rowsDeleted = db.delete(TvShowsEntry.TABLE_NAME, selection, selectionArgs);
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
        SQLiteDatabase db = myHelper.getWritableDatabase();
        long _id;
        int rowsUpdated;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                rowsUpdated = db.update(TvShowsEntry.TABLE_NAME, contentValues , selection, selectionArgs);
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
        SQLiteDatabase db = myHelper.getWritableDatabase();
        long id;
        switch(sUriMatcher.match(uri)){
            case SHOWS:
                db.beginTransaction();
                int returnCount = 0;
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
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
