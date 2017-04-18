package com.akyuu.simpleweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.akyuu.simpleweather.gson.Forecast;
import com.akyuu.simpleweather.gson.Weather;
import com.akyuu.simpleweather.util.HttpUtil;
import com.akyuu.simpleweather.util.ToastUtil;
import com.akyuu.simpleweather.util.Utility;
import com.bumptech.glide.Glide;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.bing_pic_img) ImageView mBingPicImg;
    @BindView(R.id.weather_layout) ScrollView mWeatherLayout;
    @BindView(R.id.title_city) TextView mTitleCity;
    @BindView(R.id.title_update_time) TextView mTitleUpdateTime;
    @BindView(R.id.degree_text) TextView mDegreeText;
    @BindView(R.id.weather_info_text) TextView mWeatherInfoText;
    @BindView(R.id.forecast_layout) LinearLayout mForecastLayout;
    @BindView(R.id.aqi_text) TextView mAQIText;
    @BindView(R.id.pm25_text) TextView mPM25Text;
    @BindView(R.id.comfort_text) TextView mComfortText;
    @BindView(R.id.car_wash_text) TextView mCarWashText;
    @BindView(R.id.sport_text) TextView mSportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("weather", MODE_PRIVATE);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic = preferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(mBingPicImg);
        } else {
            loadBingPic();
        }
    }

    public static Intent newIntent(Context context, String weatherId) {
        Intent intent = new Intent(context, WeatherActivity.class);
        intent.putExtra("weather_id", weatherId);
        return intent;
    }

    private void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=" + Config.KEY;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show("获取天气信息失败", Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.mStatus)) {
                            SharedPreferences.Editor editor =
                                    getSharedPreferences("weather", MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            ToastUtil.show("获取天气信息失败", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        mTitleCity.setText(weather.mBasic.mCityName);
        mTitleUpdateTime.setText(weather.mBasic.mUpdate.updateTime.split(" ")[1]);
        mDegreeText.setText(String.format("%s 度", weather.mNow.mTemperature));
        mWeatherInfoText.setText(weather.mNow.mMore.mInfo);
        mForecastLayout.removeAllViews();
        for (Forecast forecast: weather.mForecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, mForecastLayout, false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);

            dateText.setText(forecast.mDate);
            infoText.setText(forecast.mMore.mInfo);
            maxText.setText(forecast.mTemperature.mMax);
            minText.setText(forecast.mTemperature.mMin);
        }

        if (weather.mAQI != null) {
            mAQIText.setText(weather.mAQI.mCity.aqi);
            mPM25Text.setText(weather.mAQI.mCity.pm25);
        }

        String comfort = "舒适度： " + weather.mSuggestion.mComfort.mInfo;
        String carWash = "洗车指数： " + weather.mSuggestion.mCarWash.mInfo;
        String sport = "运动建议： " +weather.mSuggestion.mSport.mInfo;
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);

        mWeatherLayout.setVisibility(View.VISIBLE);
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        getSharedPreferences("weather", MODE_PRIVATE).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mBingPicImg);
                    }
                });
            }
        });
    }
}
