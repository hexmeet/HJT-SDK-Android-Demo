package com.hexmeet.hjt.event;

public class RemoteNameEvent {
    public String deviceId;
    public String name;
    public boolean isLocal;

    public RemoteNameEvent() {
    }

    public RemoteNameEvent(String deviceId, String name, boolean isLocal) {
        this.deviceId = deviceId;
        this.name = name;
        this.isLocal = isLocal;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
