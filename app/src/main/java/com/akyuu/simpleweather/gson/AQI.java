package com.akyuu.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {

    @SerializedName("city")
    public AQICity mCity;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
