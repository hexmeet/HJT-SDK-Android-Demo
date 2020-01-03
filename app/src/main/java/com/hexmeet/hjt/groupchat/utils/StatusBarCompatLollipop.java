package com.hexmeet.hjt.groupchat.utils;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.view.ViewCompat;

@TargetApi(21)
public class StatusBarCompatLollipop {
    StatusBarCompatLollipop() {
    }

    public static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        window.clearFlags(67108864);
        window.addFlags(-2147483648);
        window.setStatusBarColor(statusColor);
        window.getDecorView().setSystemUiVisibility(0);
        @SuppressLint("ResourceType") ViewGroup mContentView = (ViewGroup)window.findViewById(16908290);
        View mChildView = mContentView.getChildAt(0);
        if(mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
            ViewCompat.requestApplyInsets(mChildView);
        }

    }
}

