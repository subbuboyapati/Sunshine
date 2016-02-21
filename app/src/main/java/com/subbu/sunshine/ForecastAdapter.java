package com.subbu.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setVIewType(boolean type) {
        mUseTodayLayout = type;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /*
                Remember that these views are reused as needed.
             */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        int type = getItemViewType(cursor.getPosition());
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int iconId = 0;
        int fallbackIconId;
        switch (type) {
            case VIEW_TYPE_TODAY:
                fallbackIconId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                fallbackIconId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        Log.d("Subbu", Utility.getArtUrlForWeatherCondition(context, weatherId));
        Glide.with(context)
                .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(holder.image);
        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.descriptionTextView.setText(desc);
        holder.descriptionTextView.setContentDescription(context.getString(R.string.a11y_forecast, desc));

        boolean isMetric = Utility.isMetric(context);
        holder.minTemp.setText(Utility.formatTemperature(mContext, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric));
        holder.minTemp.setContentDescription(context.getString(R.string.a11y_low_temp, holder.minTemp.getText()));

        holder.maxTemp.setText(Utility.formatTemperature(mContext, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric));
        holder.maxTemp.setContentDescription(context.getString(R.string.a11y_high_temp, holder.maxTemp.getText()));

        String dateStr = Utility.getFriendlyDayString(context, (long) cursor.getDouble(ForecastFragment.COL_WEATHER_DATE));
        holder.dateView.setText(dateStr);
        holder.dateView.setContentDescription(dateStr);

//        holder.image.setImageResource(iconId);
        holder.image.setContentDescription(desc);
    }

    public static class ViewHolder {
        TextView descriptionTextView;
        TextView minTemp;
        TextView maxTemp;
        TextView dateView;
        ImageView image;

        ViewHolder(View view) {
            descriptionTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            minTemp = (TextView) view.findViewById(R.id.list_item_low_textview);
            maxTemp = (TextView) view.findViewById(R.id.list_item_high_textview);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            image = (ImageView) view.findViewById(R.id.list_item_icon);
        }
    }
}