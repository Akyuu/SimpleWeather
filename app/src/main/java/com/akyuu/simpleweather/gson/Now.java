package com.akyuu.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String mTemperature;

    @SerializedName("cond")
    public More mMore;

    public class More {
        @SerializedName("txt")
        public String mInfo;
    }
}
