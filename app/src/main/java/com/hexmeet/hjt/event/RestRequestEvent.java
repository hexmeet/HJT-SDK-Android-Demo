package com.hexmeet.hjt.event;


public class RestRequestEvent {
    public final static int EVENT_HAND_UP = 1;
    public final static int EVENT_MIC_MUTE = 2;

    public int what;
    boolean success = false;
    String failedMessage = "";

    public RestRequestEvent(int what) {
        this.what = what;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailedMessage() {
        return failedMessage;
    }

    public void setFailedMessage(String failedMessage) {
        this.failedMessage = failedMessage;
    }
}
