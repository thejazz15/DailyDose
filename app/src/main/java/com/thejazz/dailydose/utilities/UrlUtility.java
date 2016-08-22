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
    final static String SINGLE_SEARCH_URI = "http://api.tvmaze.com/singlesearch/shows?";
    final static String DATE_PARAM = "date";
    final static String COUNTRY_PARAM = "country";
    final static String QUERY_PARAM = "q";

    final static String POPULAR_URI = "http://api.themoviedb.org/3/tv/popular?";
    final static String API_KEY_PARAM = "api_key";
    final static String API_KEY = "370bd9adf388af99c0fb4fd03ffc5f2c";
    final static String IMAGE_URI = "http://image.tmdb.org/t/p/w500";

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

    public static String buildPopularUrl(){
        Uri uri = Uri.parse(POPULAR_URI).buildUpon().appendQueryParameter(API_KEY_PARAM, API_KEY).build();
        return uri.toString();
    }

    public static String buildPosterPath(String imageUrl){
        return IMAGE_URI+imageUrl;
    }

    public static String buildSingleSearchUrl(String query){
        Uri uri = Uri.parse(SINGLE_SEARCH_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, query).build();
        return uri.toString();
    }
}
