package com.hexmeet.hjt.sdk;


public class Peer {
    public static final int DIRECT_OUT = 1;
    public static final int DIRECT_IN = 2;
    private String from;
    private String number;
    private String name;
    private String password;
    private int direct;
    //private long startTime = 0L;
    private long endTime = 0L;
    private long duration = 0L;
    private boolean isVideoCall = true;

    public Peer(int direct) {
        this.direct = direct;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }



    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        //duration = endTime - startTime;
    }



    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isVideoCall() {
        return isVideoCall;
    }

    public void setVideoCall(boolean videoCall) {
        isVideoCall = videoCall;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
