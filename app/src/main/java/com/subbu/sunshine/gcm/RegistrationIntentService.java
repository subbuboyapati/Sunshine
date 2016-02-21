package com.subbu.sunshine.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.subbu.sunshine.MainActivity;
import com.subbu.sunshine.R;

/**
 * Created by subrahmanyam on 20-02-2016, 07:20 AM.
 */
public class RegistrationIntentService extends IntentService {
    private static final String LOG_TAG = RegistrationIntentService.class.getSimpleName();

    public RegistrationIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            synchronized (LOG_TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId)
                        , GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                sendRegistrationToServer(token);
                sp.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (Exception e) {

        }
    }

    private void sendRegistrationToServer(String token) {
        Log.d(LOG_TAG, "GCM Registration token" + token);
    }
}
