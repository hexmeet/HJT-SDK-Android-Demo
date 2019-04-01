package com.hexmeet.hjt.event;


public class SvcSpeakerEvent {
    private int index;
    private String siteName;

    public SvcSpeakerEvent(int index, String siteName) {
        this.index = index;
        this.siteName = siteName;
    }

    public int getIndex() {
        return index;
    }

    public String getSiteName() {
        return siteName;
    }
}
