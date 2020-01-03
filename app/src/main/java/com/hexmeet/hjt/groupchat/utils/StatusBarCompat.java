package com.hexmeet.hjt.groupchat.utils;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;


public class StatusBarCompat
{
    public StatusBarCompat() {
    }

    static int calculateStatusBarColor(int color, int alpha) {
        float a = 1.0F - (float)alpha / 255.0F;
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        red = (int)((double)((float)red * a) + 0.5D);
        green = (int)((double)((float)green * a) + 0.5D);
        blue = (int)((double)((float)blue * a) + 0.5D);
        return -16777216 | red << 16 | green << 8 | blue;
    }

    public static void setStatusBarColor(@NonNull Activity activity, @ColorInt int statusColor, int alpha) {
        setStatusBarColor(activity, calculateStatusBarColor(statusColor, alpha));
    }

    public static void setStatusBarColor(@NonNull Activity activity, @ColorInt int statusColor) {
        if(Build.VERSION.SDK_INT >= 21) {
            StatusBarCompatLollipop.setStatusBarColor(activity, statusColor);
        } else if(Build.VERSION.SDK_INT >= 19) {
            StatusBarCompatKitKat.setStatusBarColor(activity, statusColor);
        }

    }
}
