package com.hexmeet.hjt.event;


import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.sdk.Peer;

public class CallEvent {
    public static final int MRU_NORMAL = 100;
    public static final int MRU_OPERATOR_DISCONNECT = 101;
    public static final int EP_NO_PACKET_RECEIVED = 11;
    public static final int MRU_NO_PACKET_RECEIVED = 102;
    private CallState callState;
    private Peer peer;
    private String endReason;
    private int code;
    private String number;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
