package com.hexmeet.hjt.event;


import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.sdk.Peer;

public class CallEvent {
    private CallState callState;
    private Peer peer;
    private String endReason;

    public CallEvent(CallState callState) {
        this.callState = callState;
    }

    public CallState getCallState() {
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }
}
