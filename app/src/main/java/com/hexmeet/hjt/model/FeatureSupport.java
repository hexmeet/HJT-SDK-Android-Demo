package com.hexmeet.hjt.model;

public class FeatureSupport {
    public boolean contactWebPage = false;
    public boolean p2pCall = false;
    public boolean chatInConference = false;
    public boolean switchingToAudioConference = false;
    public boolean sitenameIsChangeable = false;

    public boolean isContactWebPage() {
        return contactWebPage;
    }

    public void setContactWebPage(boolean contactWebPage) {
        this.contactWebPage = contactWebPage;
    }

    public boolean isP2pCall() {
        return p2pCall;
    }

    public void setP2pCall(boolean p2pCall) {
        this.p2pCall = p2pCall;
    }

    public boolean isChatInConference() {
        return chatInConference;
    }

    public void setChatInConference(boolean chatInConference) {
        this.chatInConference = chatInConference;
    }

    public boolean isSwitchingToAudioConference() {
        return switchingToAudioConference;
    }

    public void setSwitchingToAudioConference(boolean switchingToAudioConference) {
        this.switchingToAudioConference = switchingToAudioConference;
    }

    public FeatureSupport() {
    }

    public FeatureSupport(boolean contactWebPage, boolean p2pCall, boolean chatInConference, boolean switchingToAudioConference, boolean sitenameIsChangeable) {
        this.contactWebPage = contactWebPage;
        this.p2pCall = p2pCall;
        this.chatInConference = chatInConference;
        this.switchingToAudioConference = switchingToAudioConference;
        this.sitenameIsChangeable = sitenameIsChangeable;
    }

    public boolean isSitenameIsChangeable() {
        return sitenameIsChangeable;
    }

    public void setSitenameIsChangeable(boolean sitenameIsChangeable) {
        this.sitenameIsChangeable = sitenameIsChangeable;
    }

    @Override
    public String toString() {
        return "FeatureSupport{" +
                "contactWebPage=" + contactWebPage +
                ", p2pCall=" + p2pCall +
                ", chatInConference=" + chatInConference +
                ", switchingToAudioConference=" + switchingToAudioConference +
                ", sitenameIsChangeable=" + sitenameIsChangeable +
                '}';
    }
}
