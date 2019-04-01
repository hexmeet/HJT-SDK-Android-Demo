package com.hexmeet.hjt.event;



public class ParticipantsMicMuteEvent {
    private boolean mute;
    private String participants;

    public ParticipantsMicMuteEvent(boolean mute, String participants) {
        this.mute = mute;
        this.participants = participants;
    }

    public boolean isMute() {
        return mute;
    }

    public String getParticipants() {
        return participants;
    }
}
