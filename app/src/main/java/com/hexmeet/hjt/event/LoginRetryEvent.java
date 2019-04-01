package com.hexmeet.hjt.event;

public class LoginRetryEvent {
    private boolean retry = true;

    public LoginRetryEvent() {
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }
}
