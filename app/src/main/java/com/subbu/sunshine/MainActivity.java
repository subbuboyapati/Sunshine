package com.subbu.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.subbu.sunshine.gcm.RegistrationIntentService;
import com.subbu.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    public static final String SENT_TOKEN_TO_SERVER = "sendTokenToServer";
    private static final int PLAY_SERVICES_REOLUTION_REQUEST = 9000;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = Utility.getPreferredLocation(this);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailsFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        SunshineSyncAdapter.initializeSyncAdapter(this);
        if (!checkPlayServices()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean sendToken = preferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sendToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(this, resultCode, PLAY_SERVICES_REOLUTION_REQUEST).show();
            } else {
                Log.d(LOG_TAG, "This device is not supported...");
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if (location != null && !mLocation.equals(location)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
                ff.setUseTodayLayout(!mTwoPane);
            }
            DetailsFragment df = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailsFragment.DETAIL_URI, dateUri);
            DetailsFragment detailsFragment = new DetailsFragment();
            detailsFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailsFragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class).setData(dateUri);
            startActivity(intent);
        }
    }
}
