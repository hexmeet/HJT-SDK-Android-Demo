package com.hexmeet.hjt.event;

public class LoginResultEvent {
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_WRONG_PASSWORD_TIME = -1;
    public static final int LOGIN_WRONG_PASSWORD = -2;
    public static final int LOGIN_WRONG_NET = -3;
    public static final int LOGIN_MANUAL_TRY= -4;
    public static final int LOGIN_ANONYMOUS_FAILED = -5;
    public static final int LOGIN_WRONG_INVALID_NAME = -6;
    public static final int LOGIN_WRONG_LOCATION_SERVER = -7;
    private int code;
    private String message;
    private boolean isAnonymous = false;

    public LoginResultEvent(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public LoginResultEvent(int code, String message, boolean isAnonymous) {
        this.code = code;
        this.message = message;
        this.isAnonymous = isAnonymous;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
}
