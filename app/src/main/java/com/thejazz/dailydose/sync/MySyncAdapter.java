package com.thejazz.dailydose.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.thejazz.dailydose.R;
import com.thejazz.dailydose.activites.FavouritesTvDetailActivity;
import com.thejazz.dailydose.activites.MainActivity;
import com.thejazz.dailydose.data.TvShowsContract;
import com.thejazz.dailydose.utilities.UrlUtility;
import com.thejazz.dailydose.utilities.Utility;
import com.thejazz.dailydose.utilities.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TheJazz on 22/08/16.
 */
public class MySyncAdapter extends AbstractThreadedSyncAdapter {

    private RequestQueue requestQueue;
    private JsonObjectRequest objectRequest, detailRequest;
    private VolleySingleton volleySingleton;
    private ImageLoader imageLoader;
    private Bitmap icon;

    public final String LOG_TAG = MySyncAdapter.class.getSimpleName();
    // Interval at which to sync with the shows, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 15;
    private static int SHOW_NOTIFICATION_ID = 3004;


    private static final String[] NOTIFY_SHOW_PROJECTION = new String[]{
            TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME,
            TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME,
            TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_MEDIUM,
            TvShowsContract.FavsShowEntry._ID
    };

    // these indices must match the projection
    private static final int INDEX_SHOW_NAME = 0;
    private static final int INDEX_EPISODE_NAME = 1;
    private static final int INDEX_IMG_URL = 2;
    private static final int INDEX_ID = 3;

    public MySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        imageLoader = volleySingleton.getImageLoader();
        Log.v(LOG_TAG, "Started service");
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Cursor cursor = getContext().getContentResolver().query(TvShowsContract.FavsShowEntry.CONTENT_URI,
                new String[]{TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME},
                null,
                null,
                null);
        Log.v(LOG_TAG, "Cursor length = " + cursor.getCount());
        String showName;
        if (cursor.moveToFirst()) {
            do {
                showName = cursor.getString(cursor.getColumnIndex(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
                Log.v(LOG_TAG, UrlUtility.buildSingleSearchUrl(showName));
                sendSyncRequest(UrlUtility.buildSingleSearchUrl(showName));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void sendSyncRequest(String url) {
        objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ContentValues values = new ContentValues();
                    String showId, showName, imgUrl, imgUrlOrg;
                    showId = Integer.toString(response.getInt("id"));
                    showName = response.getString("name");
                    imgUrl = Utility.checkJSONObjectIsNull(response, "image", "medium");
                    imgUrlOrg = Utility.checkJSONObjectIsNull(response, "image", "original");
                    JSONObject external_links = response.getJSONObject("externals");
                    String imdbId = external_links.getString("imdb");
                    String showSummary = response.getString("summary");
                    JSONObject network = response.getJSONObject("network");
                    String networkName = network.getString("name");

                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_ID, showId);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME, showName);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_MEDIUM, imgUrl);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMG_URL_ORIGINAL, imgUrlOrg);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_IMDB_ID, imdbId);
                    values.put(TvShowsContract.FavsShowEntry.COLUMN_SHOW_SUMMARY, showSummary);

                    JSONObject linksObject = response.getJSONObject("_links");
                    String nextEpisodeUrl = Utility.checkIfShowHasNextEpisode(linksObject, "nextepisode");
                    if (nextEpisodeUrl.equals("N/A")) {
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, Long.MAX_VALUE);
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, "N/A");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, "N/A");
                        Log.v(LOG_TAG, "No Next Episode");
                        updateFavourite(values);
                    } else {
                        String parts[] = nextEpisodeUrl.split("/episodes/");
                        values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_ID, parts[1]);
                        Log.v(LOG_TAG, "Next Episode " + parts[1]);
                        sendDetailEpisodeRequest(UrlUtility.buildEpisodeUrl(parts[1]), values);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(objectRequest);
    }


    public void sendDetailEpisodeRequest(String url, final ContentValues values) {
        Log.v(LOG_TAG, "Episode URL " + url);

        detailRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(LOG_TAG, "Received response");
                parseSyncResponse(response, values);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext().getApplicationContext(), "No Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(detailRequest);
    }


    private void parseSyncResponse(JSONObject object, ContentValues values) {
        String airtime, season, epiNumber, epiName, summary;
        long airdate;
        try {
            epiName = object.getString("name");
            season = Integer.toString(object.getInt("season"));
            epiNumber = Integer.toString(object.getInt("number"));
            airdate = Utility.getMillisFromStringDate(object.getString("airdate"));
            Log.v(LOG_TAG, Utility.getDateFromMillis(airdate));
            airtime = object.getString("airtime");
            summary = object.getString("summary");
            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NAME, epiName);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_SEASON, season);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_EPISODE_NUM, epiNumber);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_DATE, airdate);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_AIR_TIME, airtime);
            values.put(TvShowsContract.FavsShowEntry.COLUMN_SUMMARY, summary);
            updateFavourite(values);
        } catch (JSONException e) {
            Log.e("VOLLEY DETAILS ERROR", "Could not process details json.");
            e.printStackTrace();
        }
    }

    private void updateFavourite(ContentValues values) {
        Log.v(LOG_TAG, "Updating.. with show name = " + values.getAsString(TvShowsContract.FavsShowEntry.COLUMN_SHOW_NAME));
        Uri uri;
        uri = getContext().getContentResolver().insert(TvShowsContract.FavsShowEntry.CONTENT_URI, values);
        if (uri != null) {
            Log.v(LOG_TAG, "Return uri = " + uri.toString() + " - Favourites Updated Successfully!");
        }
        notifyFavourites();
    }

    private void notifyFavourites() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);
            Date date = new Date();
            date.setTime(lastSync);
            Log.v(LOG_TAG, "Last Sync - "+lastSync + " Date is "+new SimpleDateFormat("yyyy-MM-DD HH:MM:SS.SSS").format(date));
            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the show info.
                Uri uri = TvShowsContract.FavsShowEntry.buildShowUriWithTodayDate();

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(uri, NOTIFY_SHOW_PROJECTION, null, null, null);
                Log.v(LOG_TAG, " Today Cursor Length = "+cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        String showName = cursor.getString(INDEX_SHOW_NAME);
                        String episodeName = cursor.getString(INDEX_EPISODE_NAME);
                        Log.v(LOG_TAG, showName+" airs today!");

                        int iconId = getContext().getResources().getIdentifier("ic_launcher", "mipmap", getContext().getPackageName());
                        // Define the text of the forecast.
                        String contentText =context.getString(R.string.format_notification);

                        // NotificationCompatBuilder is a very convenient way to build backward-compatible
                        // notifications.  Just throw in some data.

                        String img_url = cursor.getString(INDEX_IMG_URL);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getContext())
                                        .setSmallIcon(iconId)
                                        .setContentTitle(showName)
                                        .setContentText(contentText)
                                        .setDefaults(Notification.DEFAULT_SOUND);

                        // Make something interesting happen when the user clicks on the notification.
                        // In this case, opening the app is sufficient.
                        Intent resultIntent = new Intent(context, FavouritesTvDetailActivity.class).putExtra("_ID", cursor.getInt(INDEX_ID));;

                        // The stack builder object will contain an artificial back stack for the
                        // started Activity.
                        // This ensures that navigating backward from the Activity leads out of
                        // your application to the Home screen.
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager =
                                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        // SHOW_NOTIFICATION_ID allows you to update the notification later on.
                        mNotificationManager.notify(SHOW_NOTIFICATION_ID++, mBuilder.build());

                    }while(cursor.moveToNext());
                }else{
                    Log.v(LOG_TAG, "No shows today!");
                }
                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MySyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
