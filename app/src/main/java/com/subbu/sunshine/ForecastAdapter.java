package com.subbu.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.subbu.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private static Cursor mCursor;
    private static ForecastOnClickHandler mClickHandler;
    private final Context mContext;
    private final View emptyView;
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, View emptyView, ForecastOnClickHandler clickHandler) {
        mContext = context;
        this.emptyView = emptyView;
        this.mClickHandler = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_TYPE_FUTURE_DAY:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to recyclerview");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
        }
        ViewCompat.setTransitionName(holder.mIconView, "iconView" + position);
        if (Utility.usingLocalGraphics(mContext)) {
            holder.mIconView.setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(holder.mIconView);
            String desc = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
            holder.mDescriptionView.setText(desc);
            holder.mDescriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, desc));
            holder.mIconView.setContentDescription(desc);

            boolean isMetric = Utility.isMetric(mContext);
            holder.mLowTempView.setText(Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric));
            holder.mLowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, holder.mLowTempView.getText()));

            holder.mHighTempView.setText(Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric));
            holder.mHighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, holder.mHighTempView.getText()));

            String dateStr = Utility.getFriendlyDayString(mContext, (long) mCursor.getDouble(ForecastFragment.COL_WEATHER_DATE));
            holder.mDateView.setText(dateStr);
            holder.mDateView.setContentDescription(dateStr);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor != null && newCursor.getCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.INVISIBLE);
        }
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setVIewType(boolean type) {
        mUseTodayLayout = type;
    }

    public static interface ForecastOnClickHandler {
        void onClick(Long date, ViewHolder holder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        ViewHolder(View view) {
            super(view);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
        }
    }
}