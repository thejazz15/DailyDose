package com.thejazz.dailydose.utilities;

import android.content.Context;
import android.content.SharedPreferences;
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

    public static String checkJSONObjectIsNull(JSONObject obj, String stringObj) {
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
                retString = newObj.getString("medium");
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

    public static String formatAirDate(String date) {
        Log.v("DATE FORMAT", "Formatting data.");
        if (date.equals("N/A"))
            return "N/A";
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String retDate = null;
        if (todayDate.equals(date))
            retDate = "Today";
        else {
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date1 = myFormat.parse(todayDate);
                Date date2 = myFormat.parse(date);
                long diff = date2.getTime() - date1.getTime();
                long numDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (numDays == 1)
                    retDate = "Tomorrow";
                else if (numDays == -1)
                    retDate = "Yesterday";
                else if (numDays < -1)
                    retDate = (numDays * -1) + " Days back";
                else
                    retDate = "In " + numDays + " Days";
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

    public static String VolleyErrorMessage(VolleyError error){
        String message = null;
        if(error instanceof TimeoutError || error instanceof NoConnectionError)
            message = "Network Timeout!";
        else if(error instanceof AuthFailureError)
            message = "Authentication Failed.";
        else if(error instanceof ServerError)
            message = "Server Request Failed.";
        else if(error instanceof NetworkError)
            message = "No network connectivity.";
        else if(error instanceof ParseError)
            message = "Could not parse data.";
        return message;
    }
}
