package com.hexmeet.hjt.widget;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundPlayer {
    private static SoundPool mSoundPlayer = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
    private static int currentStreamId;
    private static Map<String, Integer> map_type_soundId = new HashMap<String, Integer>();

    public static void init(Context context) {
        try {
            AssetManager am = context.getAssets();
            map_type_soundId.put("SOUND_INCOMING", mSoundPlayer.load(am.openFd("audiofiles/incoming.wav"), 1));
            map_type_soundId.put("SOUND_DIALING", mSoundPlayer.load(am.openFd("audiofiles/ringing.wav"), 1));
            map_type_soundId.put("SOUND_CLOSED", mSoundPlayer.load(am.openFd("audiofiles/closed.wav"), 1));
        } catch (IOException e) {
        }
    }

    public static void play(String type, boolean loop) {
        currentStreamId = mSoundPlayer.play(map_type_soundId.get(type), 1, 1, 0, loop ? -1 : 0, 1);
    }

    public static void stop() {
        mSoundPlayer.stop(currentStreamId);
    }

}