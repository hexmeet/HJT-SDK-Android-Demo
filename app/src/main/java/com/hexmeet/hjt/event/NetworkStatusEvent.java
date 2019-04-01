package com.hexmeet.hjt.event;

public class NetworkStatusEvent {
    private float network;

    public float getNetwork() {
        return network;
    }

    public NetworkStatusEvent(float network) {
        this.network = network;
    }
}
