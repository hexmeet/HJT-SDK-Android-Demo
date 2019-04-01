package com.hexmeet.hjt.sdk;


import android.os.Parcel;
import android.os.Parcelable;

public class ChannelStatList implements Parcelable {
    public MediaStatistics media_statistics;
    public SignalStatistics signal_statistics;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(media_statistics, flags);
        dest.writeParcelable(signal_statistics, flags);
    }

    public static final Creator<ChannelStatList> CREATOR = new Creator<ChannelStatList>() {

        @Override
        public ChannelStatList createFromParcel(Parcel source) {
            ChannelStatList info = new ChannelStatList();
            info.media_statistics  = source.readParcelable(MediaStatistics.class.getClassLoader());
            info.signal_statistics  = source.readParcelable(SignalStatistics.class.getClassLoader());
            return info;
        }

        @Override
        public ChannelStatList[] newArray(int size) {
            return new ChannelStatList[size];
        }
    };
}