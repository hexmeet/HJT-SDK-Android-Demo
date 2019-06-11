package com.hexmeet.hjt.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class ResourceUtils {
    private Logger LOG = Logger.getLogger(this.getClass());
    public static int screenWidth = 0, screenHeight = 0, horizontalMargin = 0
            , verticalMargin = 0, originScreenWidth = 0, originScreenHeight = 0;

    private static ResourceUtils instance = null;
    public static final int CALL_ERROR = 0;
    public static final int CALL_ERROR_9 = 9;
    public static final int CALL_ERROR_1001 = 1001;
    public static final int CALL_ERROR_10009 = 10009;
    public static final int CALL_ERROR_2001 = 2001;
    public static final int CALL_ERROR_2005 = 2005;
    public static final int CALL_ERROR_2023 = 2023;
    public static final int CALL_ERROR_2031 = 2031;
    public static final int CALL_ERROR_2033 = 2033;
    public static final int CALL_ERROR_2035 = 2035;
    public static ResourceUtils getInstance() {
        if (instance == null) {
            instance = new ResourceUtils();
        }

        return instance;
    }

    private ResourceUtils(){}

    public void initScreenSize() {
        WindowManager windowManager =
                (WindowManager) HjtApp.getInstance().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            // include navigation bar
            display.getRealSize(outPoint);
        } else {
            // exclude navigation bar
            display.getSize(outPoint);
        }
        originScreenWidth = Math.max(outPoint.x, outPoint.y);
        originScreenHeight = Math.min(outPoint.x, outPoint.y);

        LOG.info("originScreenWidth : ["+originScreenWidth+"], originScreenHeight: ["+originScreenHeight+"]");

        if((9 * originScreenWidth) > (16 * originScreenHeight)) {
            screenWidth = (originScreenHeight * 16) / 9;
            screenHeight = originScreenHeight;
            horizontalMargin = (originScreenWidth - screenWidth)/2;
            verticalMargin = 0;
        } else if((9 * originScreenWidth) < (16 * originScreenHeight)){
            screenHeight = (originScreenWidth * 9) / 16;
            screenWidth = originScreenWidth;
            horizontalMargin = 0;
            verticalMargin = (originScreenHeight - screenHeight)/2;
        } else {
            screenWidth = originScreenWidth;
            screenHeight = originScreenHeight;
            horizontalMargin = 0;
            verticalMargin = 0;
        }
        LOG.info("screenWidth : ["+screenWidth+"], screenHeight: ["+screenHeight+"], horizontalMargin: ["+horizontalMargin+"], verticalMargin: ["+verticalMargin+"]");
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    @SuppressLint("NewApi")
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }

    }

    public String getCallFailedReason(int code) {
        if(code == CALL_ERROR_1001 ) {
            return HjtApp.getInstance().getString(R.string.call_error_1001);
        } else if(code == CALL_ERROR_2005) {
            return HjtApp.getInstance().getString(R.string.call_error_2005);
        } else if(code == CALL_ERROR_2001) {
            return HjtApp.getInstance().getString(R.string.call_error_2001);
        } else if(code == CALL_ERROR_2023) {
            return HjtApp.getInstance().getString(R.string.call_error_2023);
        }else if(code == CALL_ERROR_2031){
            return HjtApp.getInstance().getString(R.string.call_error_2031);
        }else if(code == CALL_ERROR_2033){
            return HjtApp.getInstance().getString(R.string.call_error_2033);
        }else if(code == CALL_ERROR_2035){
            return HjtApp.getInstance().getString(R.string.call_error_2035);
        }else if(code == CALL_ERROR || code == CALL_ERROR_9){
            return null;
        } else {
            /*int startIndex = message.indexOf("[");
            if(startIndex >= 0) {
                int endIndex = message.indexOf("]");
                if(endIndex > startIndex) {
                    String errorCode = message.substring(startIndex + 1, endIndex);
                    return HjtApp.getInstance().getString(R.string.call_again) + " [ "+errorCode+" ] ";
                }
            }*/
            return HjtApp.getInstance().getString(R.string.call_again )+ "[ "+code+" ] ";
        }
    }
}

