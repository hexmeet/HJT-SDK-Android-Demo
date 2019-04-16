package com.hexmeet.hjt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.hexmeet.hjt.login.LoginService;

import org.apache.log4j.Logger;

import java.util.Locale;

public class ScreenReceiver extends BroadcastReceiver {
    private static Logger LOG = Logger.getLogger(ScreenReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("ScreenReceiver : "+intent.getAction().toString());
        if (intent == null)
            return;

        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            LOG.info("action screen on : "+intent.getAction());
            LoginService.getInstance().autoLogin();
        } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            HjtApp.setScreenLocked(false);
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            HjtApp.setScreenLocked(true);
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())) {
            LOG.info("locale change to " + Locale.getDefault().getLanguage());
            LoginService.getInstance().autoLogin();
        }
    }

    public boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            return true;
        }
        return false;
    }
}