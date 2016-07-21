package com.thejazz.dailydose.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TheJazz on 19/07/16.
 */
public class TvShowsContract {

    public static final String CONTENT_AUTHORITY = "com.thejazz.dailydose";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_SHOWS = "shows";

    public static final class TvShowsEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOWS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SHOWS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SHOWS;

        public static final String TABLE_NAME = "tv_shows";
        public static final String COLUMN_SHOW_ID = "show_id";
        public static final String COLUMN_SHOW_NAME = "show_name";
        public static final String COLUMN_AIR_DATE = "_air_date";
        public static final String COLUMN_SEASON = "season_num";
        public static final String COLUMN_EPISODE_NUM = "episode_num";
        public static final String COLUMN_EPISODE_NAME = "episode_name";
        public static final String COLUMN_COUNTRY_CODE = "country_code";
        public static final String COLUMN_NETWORK = "network_name";
        public static final String COLUMN_IMG_URL = "img_url";

        public static Uri buildShowUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildShowWithShowId(String show_id){
            return CONTENT_URI.buildUpon().appendPath(show_id).build();
        }

        public static Uri buildShowsWithCountry(String country_code){
            return CONTENT_URI.buildUpon().appendPath(country_code).build();
        }

        public static Uri buildShowsWithCountryAndData(String country_code, String date){
            return CONTENT_URI.buildUpon().appendPath(country_code).appendPath(date).build();
        }

        public static String getCountryCodeFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getShowIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }
}
