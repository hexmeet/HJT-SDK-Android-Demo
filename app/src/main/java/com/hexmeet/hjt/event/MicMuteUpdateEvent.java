package com.hexmeet.hjt.event;


public class MicMuteUpdateEvent {
    private String participants;

    public MicMuteUpdateEvent(String participants) {
        this.participants = participants;
    }

    public String getParticipants() {
        return participants;
    }
}
