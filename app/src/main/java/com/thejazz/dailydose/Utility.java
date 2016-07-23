package com.thejazz.dailydose;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TheJazz on 21/07/16.
 */
public class Utility {

    public static String checkFieldIsNull(JSONObject obj, String field){
        String retString = null;
        if(obj.isNull(field))
            retString = "";
        else
            try {
                retString = Integer.toString(obj.getInt(field));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return retString;
    }

    public static String checkJSONObjectIsNull(JSONObject obj, String stringObj){
        JSONObject newObj = null;
        String retString = null;
        try {
            newObj = obj.getJSONObject(stringObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(obj.isNull(stringObj))
            retString = null;
        else{
            try {
                retString = newObj.getString("medium");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return retString;
    }

    public static String getPrefferedCountry(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_country),
                context.getString(R.string.pref_default_country));
    }

    public static String formatAirDate(String date){
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String tomorrowDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(new Date().getTime() + (1000 * 60 * 60 * 24)));
        String retDate = date;
        if(todayDate.equals(date))
            retDate = "Today";
        else if(date.equals(tomorrowDate))
            retDate = "Tomorrow";
        else{
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            try {
                Date startDate = df.parse(date);
                SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE"); // the day of the week spelled out completely
                retDate = simpleDateformat.format(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return retDate;
    }
}
