package com.hexmeet.hjt.event;

public class ContentEvent {
    boolean withContent = false;

    public ContentEvent(boolean withContent) {
        this.withContent = withContent;
    }

    public boolean withContent() {
        return withContent;
    }
}
