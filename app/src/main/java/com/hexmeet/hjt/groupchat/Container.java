package com.hexmeet.hjt.groupchat;

import android.app.Activity;

public class Container {
    public final Activity activity;
    public final String account;

    public Container(Activity activity, String account) {
        this.activity = activity;
        this.account = account;
    }
}
