package com.subbu.sunshine.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by subrahmanyam on 20-02-2016, 07:28 AM.
 */
public class MyInstanceIdListenerService extends InstanceIDListenerService {
    private static final String LOG = MyInstanceIdListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
