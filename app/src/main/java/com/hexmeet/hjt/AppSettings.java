package com.hexmeet.hjt;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AppSettings {
    private static final String APP_SETTINGS = "app_settings";
    private static final String DEFAULT_STR = "";
    private static AppSettings instance;
    private SharedPreferences sp;
    private boolean isSpeakerMode;
    private boolean autoAnswer;

    private AppSettings() {
        init(HjtApp.getInstance().getContext());
    }

    interface Key {
        String SPEAKER_LAYOUT = "layout_mode_speaker";
        String AUTO_ANSWER = "auto_answer";
    }

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    private void init(Context context) {
        sp = context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        isSpeakerMode = sp.getBoolean(Key.SPEAKER_LAYOUT, false);
        autoAnswer = sp.getBoolean(Key.AUTO_ANSWER, false);
    }

    public boolean isSpeakerMode() {
        return isSpeakerMode;
    }

    public void setSpeakerMode(boolean speakerMode) {
        Log.i("isSpeaker : [",speakerMode+"]");
        if (isSpeakerMode ^ speakerMode) {
            this.isSpeakerMode = speakerMode;
            sp.edit().putBoolean(Key.SPEAKER_LAYOUT, speakerMode).apply();
        }
    }

    public boolean isAutoAnswer() {
        return autoAnswer;
    }

    public void setAutoAnswer(boolean autoAnswer) {
        if (this.autoAnswer ^ autoAnswer) {
            this.autoAnswer = autoAnswer;
            sp.edit().putBoolean(Key.AUTO_ANSWER, autoAnswer).apply();
        }
    }

}
