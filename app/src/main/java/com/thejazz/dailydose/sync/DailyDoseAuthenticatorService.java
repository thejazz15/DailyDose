package com.thejazz.dailydose.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by TheJazz on 22/08/16.
 */
public class DailyDoseAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private DailyDoseAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new DailyDoseAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
