package com.hexmeet.hjt.event;


public class RemoteMuteEvent {
    boolean muteFromMru;

    public RemoteMuteEvent(boolean muteFromMru) {
        this.muteFromMru = muteFromMru;
    }

    public boolean isMuteFromMru() {
        return muteFromMru;
    }
}
