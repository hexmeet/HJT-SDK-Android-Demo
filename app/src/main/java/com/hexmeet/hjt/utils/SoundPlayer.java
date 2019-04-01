package com.hexmeet.hjt.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundPlayer {
    private static final String tag = "SoundPlayer";
    private static SoundPool mSoundPlayer = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
    private static int currentStreamId;
    private static Map<String, Integer> map_type_soundId = new HashMap<String, Integer>();

    public static void init(Context context) {
        try {
            AssetManager am = context.getAssets();
            map_type_soundId.put("incoming", mSoundPlayer.load(am.openFd("audiofiles/ringtone.wav"), 1));
            //map_type_soundId.put("dialing", mSoundPlayer.load(am.openFd("audiofiles/ringing.wav"), 1));
            //map_type_soundId.put("hold", mSoundPlayer.load(am.openFd("audiofiles/hold.wav"), 1));
            //map_type_soundId.put("closed", mSoundPlayer.load(am.openFd("audiofiles/closed.wav"), 1));
        } catch (IOException e) {
        }
    }

    public static void play(String type, boolean loop) {
        currentStreamId = mSoundPlayer.play(map_type_soundId.get(type), 1, 1, 0, loop ? -1 : 0, 1);
        Log.i(tag, "play type: " + type + ", loop: " + loop + ", currentStreamId: " + currentStreamId);
    }

    public static void stop() {
        Log.i(tag, "stop currentStreamId: " + currentStreamId);
        mSoundPlayer.stop(currentStreamId);
    }

}