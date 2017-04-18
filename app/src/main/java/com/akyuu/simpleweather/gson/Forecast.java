package com.akyuu.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    @SerializedName("date")
    public String mDate;

    @SerializedName("cond")
    public More mMore;

    @SerializedName("tmp")
    public Temperature mTemperature;

    public class More {
        @SerializedName("txt")
        public String mInfo;
    }

    public class Temperature {
        @SerializedName("max")
        public String mMax;
        @SerializedName("min")
        public String mMin;
    }
}
