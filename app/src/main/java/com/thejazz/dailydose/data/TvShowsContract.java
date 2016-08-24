package com.thejazz.dailydose.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TheJazz on 19/07/16.
 */
public class TvShowsContract {

    public static final String CONTENT_AUTHORITY = "com.thejazz.dailydose";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SHOWS = "shows";
    public static final String FAV_SHOWS = "favs";

    public static final class TvShowsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHOWS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOWS;

        public static final String TABLE_NAME = "episodes";
        public static final String COLUMN_SHOW_ID = "show_id";
        public static final String COLUMN_SHOW_NAME = "show_name";
        public static final String COLUMN_AIR_DATE = "_air_date";
        public static final String COLUMN_SEASON = "season_num";
        public static final String COLUMN_EPISODE_NUM = "episode_num";
        public static final String COLUMN_EPISODE_ID = "episode_id";
        public static final String COLUMN_EPISODE_NAME = "episode_name";
        public static final String COLUMN_COUNTRY_CODE = "country_code";
        public static final String COLUMN_NETWORK = "network_name";
        public static final String COLUMN_IMG_URL_MEDIUM = "img_url_medium";
        public static final String COLUMN_IMG_URL_ORIGINAL = "img_url_original";

        public static Uri buildShowUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
//
//        public static Uri buildShowWithShowId(String show_id) {
//            return CONTENT_URI.buildUpon().appendPath("show").appendPath(show_id).build();
//        }
//
//        public static Uri buildShowsWithCountry(String country_code) {
//            return CONTENT_URI.buildUpon().appendPath(country_code).build();
//        }

        public static Uri buildShowsWithCountryAndDate(String country_code, long date) {
            return CONTENT_URI.buildUpon().appendPath(country_code).appendPath(Long.toString(date)).build();
        }

//        public static Uri buildShowWithEpisodeId(String episodeId) {
//            return CONTENT_URI.buildUpon().appendPath("episode").appendPath("id").appendPath(episodeId).build();
//        }

        public static String getCountryCodeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getShowIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getEpisodeIdFromUri(Uri uri) {
            return uri.getPathSegments().get(3);
        }
    }



    public static final class FavsShowEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(FAV_SHOWS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FAV_SHOWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FAV_SHOWS;

        public static final String TABLE_NAME = "favs";
        public static final String COLUMN_SHOW_ID = "show_id";
        public static final String COLUMN_SHOW_NAME = "show_name";
        public static final String COLUMN_AIR_DATE = "_air_date";
        public static final String COLUMN_AIR_TIME = "_air_time";
        public static final String COLUMN_SEASON = "season_num";
        public static final String COLUMN_EPISODE_NAME = "episode_name";
        public static final String COLUMN_EPISODE_NUM = "episode_num";
        public static final String COLUMN_EPISODE_ID = "episode_id";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_SHOW_SUMMARY = "show_summary";
        public static final String COLUMN_IMG_URL_MEDIUM = "img_url_medium";
        public static final String COLUMN_IMG_URL_ORIGINAL = "img_url_original";
        public static final String COLUMN_IMDB_ID = "imdb_id";

        public static Uri buildShowUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildShowUriWithTodayDate(){
            return CONTENT_URI.buildUpon().appendPath("today").appendPath("favs").appendPath("shows").build();
        }

        public static Uri buildShowWithShowId(String show_id) {
            return CONTENT_URI.buildUpon().appendPath("show").appendPath(show_id).build();
        }

        public static String getShowIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}
