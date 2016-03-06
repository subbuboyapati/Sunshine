package com.subbu.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.subbu.sunshine.data.WeatherContract;
import com.subbu.sunshine.sync.SunshineSyncAdapter;

import static com.subbu.sunshine.data.WeatherContract.LocationEntry;
import static com.subbu.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by subrahmanyam on 21-11-2015.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, ForecastAdapter.ForecastOnClickHandler {

    public static final int LOADER_ID = 100;
    final static int COL_WEATHER_ID = 0;
    final static int COL_WEATHER_DATE = 1;
    final static int COL_WEATHER_DESC = 2;
    final static int COL_WEATHER_MAX_TEMP = 3;
    final static int COL_WEATHER_MIN_TEMP = 4;
    final static int COL_LOCATION_SETTING = 5;
    final static int COL_WEATHER_CONDITION_ID = 6;
    final static int COL_COORD_LAT = 7;
    final static int COL_COORD_LONG = 8;
    private static final String LOG = ForecastFragment.class.getSimpleName();
    final String[] FORECAST_COLUMNS = new String[]{
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };
    private ForecastAdapter mForecastAdapter;
    private RecyclerView recyclerView;
    private LinearLayout parallaxBar;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean mUseTodayLayout;
    private TextView emptyView;
    private boolean mHoldForTransition;

    public ForecastFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setUseTodayLayout(boolean layout) {
        mUseTodayLayout = layout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setVIewType(mUseTodayLayout);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, null);
        emptyView = (TextView) view.findViewById(R.id.emptyView);
        recyclerView = (RecyclerView) view.findViewById(R.id.listview_forecast);
        parallaxBar = (LinearLayout) view.findViewById(R.id.parallax_bar);

        if (null != parallaxBar) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int max = parallaxBar.getHeight();
                    if (dy > 0) {
                        parallaxBar.setTranslationY(Math.max(-max, parallaxBar.getTranslationY() - dy / 2));
                    } else {
                        parallaxBar.setTranslationY(Math.min(0, parallaxBar.getTranslationY() - dy / 2));
                    }
                }
            });
        }
        mForecastAdapter = new ForecastAdapter(getActivity(), emptyView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mForecastAdapter);
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("position");
        }
        mHoldForTransition = false;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
        if (mHoldForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != recyclerView) {
            recyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_map:
                openPreferredLocationInMap();
        }
        return false;
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (null != mForecastAdapter) {
            Cursor c = mForecastAdapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }


    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getContext(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
//        recyclerView.smoothScrollToPosition(mPosition);
        updateEmptyView();
    }

    private void updateEmptyView() {
        TextView tv = (TextView) getView().findViewById(R.id.emptyView);
        int message = R.string.no_weather_info;
        if (null != tv) {
            int locationStatus = Utility.getLocationStatus(getActivity());
            switch (locationStatus) {
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                    message = R.string.empty_forecast_list_server_down;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                    message = R.string.empty_forecast_list_server_error;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                    message = R.string.empty_forecast_list_invalid_location;
                default:
                    if (!Utility.isNetworkAvailable(getActivity()))
                        message = R.string.no_network;
                    break;
            }
        }
        tv.setText(message);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.location_status))) {
            updateWeather();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mPosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onClick(Long date, ForecastAdapter.ViewHolder holder) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, date), holder);
        mPosition = holder.getAdapterPosition();
    }

    public interface Callback {
        void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder viewHolder);
    }
}
