package com.hexmeet.hjt.event;

public class MuteSpeaking {
    public boolean muteDetected;

    public MuteSpeaking(boolean muteDetected) {
        this.muteDetected = muteDetected;
    }
    public boolean isMuteSpeaking() {
        return muteDetected;
    }
}
