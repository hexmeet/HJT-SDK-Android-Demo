package com.hexmeet.hjt.event;

public class EmLoginSuccessEvent {
    boolean loginSucceed;

    public boolean isLoginSucceed() {
        return loginSucceed;
    }

    public void setLoginSucceed(boolean loginSucceed) {
        this.loginSucceed = loginSucceed;
    }

    public EmLoginSuccessEvent(boolean loginSucceed) {
        this.loginSucceed = loginSucceed;
    }
}
