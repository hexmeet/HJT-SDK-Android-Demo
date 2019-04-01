package com.hexmeet.hjt.sdk;

import android.os.Parcel;
import android.os.Parcelable;


public class ChannelStatistics implements Parcelable {
    public String codec;
    public String participantName;
    public String pipeName;
    public String resolution;
    public int frameRate;
    public int jitter;
    public int packetLost;
    public int packetLostRate;
    public int rtp_actualBitRate;
    public int rtp_settingBitRate;
    public int totalPacket;
    public boolean encrypted;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(codec);
        dest.writeString(participantName);
        dest.writeString(pipeName);
        dest.writeString(resolution);
        dest.writeInt(frameRate);
        dest.writeInt(jitter);
        dest.writeInt(packetLost);
        dest.writeInt(packetLostRate);
        dest.writeInt(rtp_actualBitRate);
        dest.writeInt(rtp_settingBitRate);
        dest.writeInt(totalPacket);
        dest.writeInt(encrypted ? 1 : 0);
    }
    
    public static final Creator<ChannelStatistics> CREATOR = new Creator<ChannelStatistics>() {

        @Override
        public ChannelStatistics createFromParcel(Parcel source) {
            ChannelStatistics info = new ChannelStatistics();
            info.codec = source.readString();
            info.participantName = source.readString();
            info.pipeName = source.readString();
            info.resolution = source.readString();
            info.frameRate = source.readInt();
            info.jitter = source.readInt();
            info.packetLost = source.readInt();
            info.packetLostRate = source.readInt();
            info.rtp_actualBitRate = source.readInt();
            info.rtp_settingBitRate = source.readInt();
            info.totalPacket = source.readInt();
            info.encrypted = (source.readInt() == 1);

            return info;
        }

        @Override
        public ChannelStatistics[] newArray(int size) {
            return new ChannelStatistics[size];
        }
    };
}