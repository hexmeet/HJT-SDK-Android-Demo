package com.hexmeet.hjt.event;

public class ServerReachableEvent {
    boolean reachable;

    public ServerReachableEvent(boolean reachable) {
        this.reachable = reachable;
    }

    public boolean isReachable() {
        return reachable;
    }
}
