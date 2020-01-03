package com.hexmeet.hjt.event;

public class RemoteNameUpdateEvent {
    public int window;
    public String name;
    public String deviceId;
    public boolean isLocal;

    public int getWindow() {
        return window;
    }

    public String getName() {
        return name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public RemoteNameUpdateEvent(int window, String name, String deviceId, boolean isLocal) {
        this.window = window;
        this.name = name;
        this.deviceId = deviceId;
        this.isLocal = isLocal;
    }
}
