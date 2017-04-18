package com.akyuu.simpleweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private Fragment getFragment() {
        return ChooseAreaFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("weather", MODE_PRIVATE);
        if (preferences.getString("weather", null) != null) {
            Intent i = WeatherActivity.newIntent(this, preferences.getString("weather", null));
            startActivity(i);
            finish();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container);

        if (fragment == null) {
            fragment = getFragment();
            fm.beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }

    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container);

        if (fragment instanceof BackKeyFragment) {
            ((BackKeyFragment)fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
