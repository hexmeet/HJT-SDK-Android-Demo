package com.hexmeet.hjt;


public interface AppCons {
    String USER_AGENT = "HexMeet EasyVideo Android";
    String SYSTEM_CALLING_ACTION = "hexmeet.software.hjt.calling";
    String APP_FILE_PROVIDER_AUTH = BuildConfig.APPLICATION_ID + ".fileprovider";
    String BLUETOOTH_CONNECT_ACTION = "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED";
    String BLUETOOTH_STATE_CHNGED = "android.bluetooth.adapter.action.STATE_CHANGED";

    String INTENT_KEY_WEB_INVITE = "key_web_invite";
    int INTENT_VALUE_WEB_INVITE_DIAOUT = 10;
    int INTENT_VALUE_WEB_INVITE_ANONYMOUS = 11;

    int MAX_RECEIVE_STREAM = 4;
    int DEFAULT_BITRATE = 2048;
    int APP_ID = 9527;

    String HTTPS_PREFIX = "https://";
    String HTTP_PREFIX = "http://";

    String HTTP_DEFAULT_PORT = "80";
    String HTTTPS_DEFAULT_PORT = "443";

    interface  BundleKeys {
        String EXTRA_DATA = "com.hexmeet.hjt.Keys.DATA";
        String EXTRA_MESSAGE = "com.hexmeet.hjt.Keys.MESSAGE";
    }

    interface LoginType {
        int LOGIN_TYPE_PRIVATE = 1;
        int LOGIN_TYPE_CLOUD = 2;
    }
}
