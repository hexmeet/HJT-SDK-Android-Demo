package com.hexmeet.hjt.event;

public class RefreshLayoutModeEvent {
    boolean speakerMode = true;

    public boolean isSpeakerMode() {
        return speakerMode;
    }

    public RefreshLayoutModeEvent(boolean speakerMode) {
        this.speakerMode = speakerMode;
    }

    public void setSpeakerMode(boolean speakerMode) {
        this.speakerMode = speakerMode;
    }
}
