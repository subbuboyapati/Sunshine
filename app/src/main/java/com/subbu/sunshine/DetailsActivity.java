package com.subbu.sunshine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailsFragment.DETAIL_URI, getIntent().getData());
            arguments.putBoolean(DetailsFragment.DETAIL_TRANSITION_ANIMATION, true);

            DetailsFragment detailsFragment = new DetailsFragment();
            detailsFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, detailsFragment)
                    .commit();
            supportPostponeEnterTransition();
        }
    }

}
