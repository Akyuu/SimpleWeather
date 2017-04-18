package com.akyuu.simpleweather.util;

import android.widget.Toast;

import com.akyuu.simpleweather.WeatherApplication;

public class ToastUtil {
    private static Toast sToast;

    public static void show(String text, int duration) {
        if (sToast == null) {
            sToast = Toast.makeText(WeatherApplication.getContext(), text, duration);
        } else {
            sToast.setText(text);
            sToast.setDuration(duration);
        }
        sToast.show();
    }

}
