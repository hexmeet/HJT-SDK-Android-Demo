package com.hexmeet.hjt.event;

public class MicEnabledEvent {
    private boolean isMicEnabled;

    public MicEnabledEvent(boolean isMicEnabled) {
        this.isMicEnabled = isMicEnabled;
    }

    public boolean isMicEnabled() {
        return isMicEnabled;
    }
}
