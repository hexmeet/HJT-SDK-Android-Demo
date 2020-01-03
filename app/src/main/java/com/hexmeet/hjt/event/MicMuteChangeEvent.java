package com.hexmeet.hjt.event;

public class MicMuteChangeEvent {
    private boolean isMute;

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public MicMuteChangeEvent(boolean isMute) {
        this.isMute = isMute;
    }
}
