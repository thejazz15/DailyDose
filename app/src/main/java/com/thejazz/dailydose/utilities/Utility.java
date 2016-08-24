package com.thejazz.dailydose.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.data.TvShowsContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by TheJazz on 21/07/16.
 */
public class Utility {

    public static String checkFieldIsNull(JSONObject obj, String field) {
        String retString = null;
        if (obj.isNull(field))
            retString = "";
        else
            try {
                retString = Integer.toString(obj.getInt(field));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return retString;
    }

    public static String checkJSONObjectIsNull(JSONObject obj, String stringObj, String field) {
        JSONObject newObj = null;
        String retString = null;
        try {
            newObj = obj.getJSONObject(stringObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj.isNull(stringObj))
            retString = "";
        else {
            try {
                retString = newObj.getString(field);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return retString;
    }

    public static String checkIfShowHasNextEpisode(JSONObject links, String nextEpisode) {
        String retString = null;
        JSONObject newObj;
        if (links.has(nextEpisode)) {
            try {
                newObj = links.getJSONObject(nextEpisode);
                retString = newObj.getString("href");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            retString = "N/A";
        }

        return retString;
    }

    public static String getPrefferedCountry(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_country),
                context.getString(R.string.pref_default_country));
    }

    public static String formatAirsInText(String date) {
        if (date.equals("Today") || date.equals("Tomorrow") || date.startsWith("In"))
            return "Airs";
        else if (date.equals("Yesterday") || date.contains("back"))
            return "Aired";
        else
            return "";
    }

    public static String getDateFromMillis(long millis) {
        if (millis == Long.MAX_VALUE)
            return "TBA";
        Date date = new Date();
        date.setTime(millis);
        String formatDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return formatAirDate(formatDate);
    }

    public static String formatAirDate(String date) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String retDate = null;
        if (todayDate.equals(date))
            retDate = "Airs Today";
        else {
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date1 = myFormat.parse(todayDate);
                Date date2 = myFormat.parse(date);
                long diff = date2.getTime() - date1.getTime();
                long numDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (numDays == 1)
                    retDate = "Airs Tomorrow";
                else if (numDays == -1)
                    retDate = "Aired Yesterday";
                else if (numDays < -1)
                    retDate = "N/A";
                else if (numDays < -1)
                    retDate = "Aired " + (numDays * -1) + " Days back";
                else
                    retDate = "Airs " + "In " + numDays + " Days";
            } catch (ParseException e) {
                Log.e("DATE ERROR", "Error formmating date");
                e.printStackTrace();
            }
        }
        return retDate;
    }

    public static String formatNumber(String inputString) {
        if (inputString.equals(""))
            return "";
        else {
            int number = Integer.parseInt(inputString);
            if (number < 10)
                return "0" + inputString;
            else
                return inputString;
        }
    }

    public static String VolleyErrorMessage(VolleyError error) {
        String message = null;
        if (error instanceof TimeoutError || error instanceof NoConnectionError)
            message = "Network Timeout!";
        else if (error instanceof AuthFailureError)
            message = "Authentication Failed.";
        else if (error instanceof ServerError)
            message = "Server Request Failed.";
        else if (error instanceof NetworkError)
            message = "No network connectivity.";
        else if (error instanceof ParseError)
            message = "Could not parse data.";
        return message;
    }

    public static long getMillisFromStringDate(String date) {
        long millis = 0;
        Date dt;
        try {
            dt = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            millis = dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millis;
    }

    public static String formatSummary(String summary) {
        String retSummary = summary.replaceAll("<p>", "");
        retSummary = retSummary.replaceAll("</p>", "");
        retSummary = retSummary.replaceAll(";", ",");
        retSummary = retSummary.replaceAll("<em>", "");
        retSummary = retSummary.replaceAll("</em>", "");
        retSummary = retSummary.replaceAll("<strong>", "");
        retSummary = retSummary.replaceAll("</strong>", "");
        retSummary = retSummary.replaceAll("/", "");
        retSummary = retSummary.replaceAll("<span>","");
        retSummary = retSummary.replaceAll("</span>","");
        return retSummary;
    }

    public static boolean isFavourite(ContentValues values) {
        Uri uri = TvShowsContract.FavsShowEntry.buildShowWithShowId(values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID));
        Log.v("URI", "URI : " + uri);
        Cursor mCursor = MyApplication.getAppContext().getContentResolver().query(uri, null, null, null, null);
        Log.v("IS FAVOURITE?", "Search " + Integer.toString(mCursor.getCount()) + " " + values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
        if (mCursor.getCount() > 0)
            return true;
        return false;
    }

    public static boolean isFavourite(String showId) {
        Uri uri = TvShowsContract.FavsShowEntry.buildShowWithShowId(showId);
        Log.v("URI", "URI : " + uri);
        Cursor mCursor = MyApplication.getAppContext().getContentResolver().query(uri, null, null, null, null);
        if (mCursor.getCount() > 0)
            return true;
        return false;
    }

}
