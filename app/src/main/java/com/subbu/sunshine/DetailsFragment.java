package com.subbu.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.subbu.sunshine.data.WeatherContract;
import com.subbu.sunshine.data.WeatherContract.WeatherEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment implements LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private static final String LOG = DetailsFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #Sunshine";
    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID
    };
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX = 3;
    private static final int COL_WEATHER_MIN = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_DEGREE = 8;
    private static final int COL_WEATHER_WEATHER_ID = 9;
    private final int LOADER_FORECAST = 101;
    private TextView detailsDay;
    private TextView detailsDate;
    private TextView detailsMin;
    private TextView detailsMax;
    private TextView detailsDesc;
    private TextView detailsHumidity;
    private TextView detailsWind;
    private TextView detailsPressure;
    private ImageView icon;
    private ShareActionProvider mShareActionProvider;
    private String mForecastStr;
    private Uri mUri;
    private View view;
//    private MyView angleView;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailsFragment.DETAIL_URI);
        }
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_details, container, false);
            detailsDay = (TextView) view.findViewById(R.id.details_day);
            detailsDate = (TextView) view.findViewById(R.id.details_date);
            detailsMin = (TextView) view.findViewById(R.id.details_min);
            detailsMax = (TextView) view.findViewById(R.id.details_max);
            detailsDesc = (TextView) view.findViewById(R.id.details_desc);
            detailsHumidity = (TextView) view.findViewById(R.id.details_humidity);
            detailsWind = (TextView) view.findViewById(R.id.details_wind);
            detailsPressure = (TextView) view.findViewById(R.id.details_pressure);
            icon = (ImageView) view.findViewById(R.id.details_icon);
//            angleView = (MyView) view.findViewById(R.id.details_angle);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG, "Share Action Provider is null?");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_FORECAST, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG, "OnloaderFinished");
        data.moveToFirst();

        detailsDay.setText(Utility.getDayName(getContext(), (long) data.getDouble(COL_WEATHER_DATE)));
        detailsDate.setText(Utility.getFormattedMonthDay(getContext(), (long) data.getDouble(COL_WEATHER_DATE)));
        boolean isMetric = Utility.isMetric(getContext());
        String highString = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX), isMetric);
        detailsMax.setText(highString);
        detailsMax.setContentDescription(getString(R.string.a11y_high_temp, highString));

        String lowString = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN), isMetric);
        detailsMin.setText(lowString);
        detailsMin.setContentDescription(getString(R.string.a11y_low_temp, lowString));

        String description = data.getString(COL_WEATHER_DESC);
        detailsDesc.setText(description);
        detailsDesc.setContentDescription(getString(R.string.a11y_forecast, description));

        detailsHumidity.setText(String.format(getString(R.string.format_humidity), data.getDouble(COL_WEATHER_HUMIDITY)));
        detailsHumidity.setContentDescription(detailsHumidity.getText());

        String windStr = Utility.getFormattedWind(getContext(), (float) data.getDouble(COL_WEATHER_WIND)
                , (float) data.getDouble(COL_WEATHER_DEGREE));
        detailsWind.setText(windStr);
        detailsWind.setContentDescription(detailsWind.getText());

        detailsPressure.setText(String.format(getString(R.string.format_pressure), data.getDouble(COL_WEATHER_PRESSURE)));
        detailsPressure.setContentDescription(detailsPressure.getText());

//        icon.setImageResource();
        int weatherId = data.getInt(COL_WEATHER_WEATHER_ID);
        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getContext(), weatherId))
                .error(Utility.getArtResourceForWeatherCondition(weatherId))
                .crossFade()
                .into(icon);
        icon.setContentDescription(getString(R.string.a11y_forecast_icon, description));
//        angleView.setAngle(data.getLong(COL_WEATHER_WIND));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(LOADER_FORECAST, null, this);
        }
    }
}
