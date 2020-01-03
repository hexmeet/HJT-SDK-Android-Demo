package com.hexmeet.hjt.event;

public class NetworkEvent {
    public static final String EV_WARN_NETWORK_POOR = "NetworkPoor";
    public static final String EV_WARN_NETWORK_VERY_POOR = "NetworkVeryPoor";
    public static final String EV_WARN_BANDWIDTH_INSUFFICIENT = "BandwidthInsufficient";
    public static final String EV_WARN_BANDWIDTH_VERY_INSUFFICIENT = "BandwidthVeryInsufficient";
    public static final String EV_WARN_UNMUTE_AUDIO_NOT_ALLOWED = "UnmuteAudioNotAllowed";
    public static final String EV_WARN_UNMUTE_AUDIO_INDICATION = "UnmuteAudioIndication";
    private String code;

    public NetworkEvent(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
