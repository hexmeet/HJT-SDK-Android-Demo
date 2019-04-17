package com.hexmeet.hjt.dial;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hexmeet.hjt.HjtApp;

import java.util.LinkedList;

public class RecentPreference {
    private static final String LOGIN_SETTINGS = "recent_settings";
    private static final String PREFERENCE_KEY_RECENT = "recent_store";
    private static final String DEFAULT_STR = "";
    private static RecentPreference instance;
    private SharedPreferences sp;

    private LinkedList<String> recentList = new LinkedList<>();

    private RecentPreference() {
        init(HjtApp.getInstance().getContext());
    }

    public static RecentPreference getInstance() {
        if (instance == null) {
            instance = new RecentPreference();
        }
        return instance;
    }

    private void init(Context context) {
        sp = context.getSharedPreferences(LOGIN_SETTINGS, Context.MODE_PRIVATE);

        String str = sp.getString(PREFERENCE_KEY_RECENT, "");
        if(!TextUtils.isEmpty(str)) {
            if (str.contains("-")) {
                String[] recentArray = str.split("-");
                for (int i = 0; i < recentArray.length; i++) {
                    if(i >= 5) {
                        break;
                    }

                    recentList.addLast(recentArray[i]);
                }
            } else {
                recentList.addFirst(str);
            }
        }
    }

    public void updateRecent(String number, boolean add) {
        if(TextUtils.isEmpty(number)) {
            return;
        }
        if(recentList.contains(number)) {
            recentList.remove(number);
        }
        if(add) {
            recentList.addFirst(number);
            if(recentList.size() > 5) {
                recentList.removeLast();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentList.size(); i++) {
            if(i > 0) {
                sb.append("-");
            }
            sb.append(recentList.get(i));
        }
        sp.edit().putString(PREFERENCE_KEY_RECENT, sb.toString()).apply();
    }


    public LinkedList<String> getRecentList() {
        return recentList;
    }
}
