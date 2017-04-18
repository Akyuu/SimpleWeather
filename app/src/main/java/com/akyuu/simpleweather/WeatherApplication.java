package com.akyuu.simpleweather;

import android.app.Application;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;

public class WeatherApplication extends Application{

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(getApplicationContext());
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
