package com.akyuu.simpleweather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;

import com.akyuu.simpleweather.gson.Weather;
import com.akyuu.simpleweather.util.HttpUtil;
import com.akyuu.simpleweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("service".equals(intent.getStringExtra("sender"))) {
            updateWeather();
            updateBingPic();
        }

        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AutoUpdateService.class);
        i.putExtra("sender", "service");
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 8 * AlarmManager.INTERVAL_HOUR,
                pi);

        return START_STICKY;
    }

    private void updateWeather() {
        SharedPreferences preferences = getSharedPreferences("weather", MODE_PRIVATE);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.mBasic.mWeatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityId="
                    + weatherId + "&key=" + Config.KEY;
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.mStatus)) {
                        SharedPreferences.Editor editor =
                                getSharedPreferences("weather", MODE_PRIVATE).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        getSharedPreferences("weather", MODE_PRIVATE).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
