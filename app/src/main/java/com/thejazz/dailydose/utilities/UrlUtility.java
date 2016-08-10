package com.thejazz.dailydose.utilities;

import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TheJazz on 04/08/16.
 */
public class UrlUtility {
    final static String SCHEDULE_URI = "http://api.tvmaze.com/schedule?";
    final static String SEARCH_URI = "http://api.tvmaze.com/search/shows?";
    final static String EPISODE_URI = "http://api.tvmaze.com/episodes/";
    final static String DATE_PARAM = "date";
    final static String COUNTRY_PARAM = "country";
    final static String QUERY_PARAM = "q";

    public static String buildTodayUrl(String country){
        Uri uri = Uri.parse(SCHEDULE_URI).buildUpon()
                .appendQueryParameter(DATE_PARAM, new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .appendQueryParameter(COUNTRY_PARAM,country)
                .build();
        return uri.toString();
    }

    public static String buildSearchUrl(String tvShow){
        Uri uri = Uri.parse(SEARCH_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, tvShow).build();
        return uri.toString();
    }

    public static String buildEpisodeUrl(String episodeId){
        Uri uri = Uri.parse(EPISODE_URI).buildUpon().appendPath(episodeId).build();
        return uri.toString();
    }
}
