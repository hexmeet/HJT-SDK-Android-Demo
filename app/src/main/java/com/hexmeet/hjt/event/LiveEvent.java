package com.hexmeet.hjt.event;

public class LiveEvent {
    private boolean isRecording;

    public LiveEvent(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
