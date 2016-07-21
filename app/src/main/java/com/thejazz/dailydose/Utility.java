package com.thejazz.dailydose;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TheJazz on 21/07/16.
 */
public class Utility {

    public static String checkIfFieldIsNull(JSONObject obj, String field){
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
}
