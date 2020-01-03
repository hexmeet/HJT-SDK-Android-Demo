package com.hexmeet.hjt.groupchat.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;

import androidx.core.view.ViewCompat;

@TargetApi(19)
public class StatusBarCompatKitKat {
    private static final String TAG_FAKE_STATUS_BAR_VIEW = "statusBarView";
    private static final String TAG_MARGIN_ADDED = "marginAdded";

    StatusBarCompatKitKat() {
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }

        return result;
    }

    private static View addFakeStatusBarView(Activity activity, int statusBarColor, int statusBarHeight) {
        Window window = activity.getWindow();
        ViewGroup mDecorView = (ViewGroup)window.getDecorView();
        View mStatusBarView = new View(activity);
        LayoutParams layoutParams = new LayoutParams(-1, statusBarHeight);
        layoutParams.gravity = 48;
        mStatusBarView.setLayoutParams(layoutParams);
        mStatusBarView.setBackgroundColor(statusBarColor);
        mStatusBarView.setTag("statusBarView");
        mDecorView.addView(mStatusBarView);
        return mStatusBarView;
    }

    private static void removeFakeStatusBarViewIfExist(Activity activity) {
        Window window = activity.getWindow();
        ViewGroup mDecorView = (ViewGroup)window.getDecorView();
        View fakeView = mDecorView.findViewWithTag("statusBarView");
        if(fakeView != null) {
            mDecorView.removeView(fakeView);
        }

    }

    private static void addMarginTopToContentChild(View mContentChild, int statusBarHeight) {
        if(mContentChild != null) {
            if(!"marginAdded".equals(mContentChild.getTag())) {
                LayoutParams lp = (LayoutParams)mContentChild.getLayoutParams();
                lp.topMargin += statusBarHeight;
                mContentChild.setLayoutParams(lp);
                mContentChild.setTag("marginAdded");
            }

        }
    }

    public static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        window.addFlags(67108864);
        @SuppressLint("ResourceType") ViewGroup mContentView = (ViewGroup)window.findViewById(16908290);
        View mContentChild = mContentView.getChildAt(0);
        int statusBarHeight = getStatusBarHeight(activity);
        removeFakeStatusBarViewIfExist(activity);
        addFakeStatusBarView(activity, statusColor, statusBarHeight);
        addMarginTopToContentChild(mContentChild, statusBarHeight);
        if(mContentChild != null) {
            ViewCompat.setFitsSystemWindows(mContentChild, false);
        }

    }
}

