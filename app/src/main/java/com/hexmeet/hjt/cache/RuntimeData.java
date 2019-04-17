package com.hexmeet.hjt.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.utils.Utils;

import org.apache.commons.lang3.StringUtils;

public class RuntimeData {
    private static SharedPreferences sp;
    private static String deviceSN = null;

    private static SharedPreferences getSp() {
        if (sp == null) {
            return sp = HjtApp.getInstance().getContext().getSharedPreferences("runtime_data", Context.MODE_PRIVATE);
        }

        return sp;
    }

    private static void saveStr(String name, String value) {
        getSp().edit().putString(name, value).commit();
    }

    private static String getStr(String name) {
        return getSp().getString(name, "");
    }

    private static String getStr(String name, String defValue) {
        return getSp().getString(name, defValue);
    }

    public static String getDeviceSN() {
        String sn = StringUtils.isNotEmpty(deviceSN) ? deviceSN : (deviceSN = getStr("deviceSN"));
        if(TextUtils.isEmpty(sn)) {
            setDeviceSN(Utils.getUUID());
        }
        return deviceSN;
    }

    public static void setDeviceSN(String deviceSN) {
        RuntimeData.deviceSN = deviceSN;
        saveStr("deviceSN", deviceSN);
    }
}
