package com.hexmeet.hjt.sdk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MediaStatistics implements Parcelable {
    public List<ChannelStatistics> ar;
    public List<ChannelStatistics> as;
    public List<ChannelStatistics> cr;
    public List<ChannelStatistics> pr;
    public List<ChannelStatistics> ps;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(ar);
        dest.writeTypedList(as);
        dest.writeTypedList(cr);
        dest.writeTypedList(pr);
        dest.writeTypedList(ps);
    }

    public static final Creator<MediaStatistics> CREATOR = new Creator<MediaStatistics>() {

        @Override
        public MediaStatistics createFromParcel(Parcel source) {
            MediaStatistics info = new MediaStatistics();
            info.ar = new ArrayList<>();
            source.readList(info.ar, ChannelStatistics.class.getClassLoader());
            info.as = new ArrayList<>();
            source.readList(info.as, ChannelStatistics.class.getClassLoader());
            info.cr = new ArrayList<>();
            source.readList(info.cr, ChannelStatistics.class.getClassLoader());
            info.pr = new ArrayList<>();
            source.readList(info.pr, ChannelStatistics.class.getClassLoader());
            info.ps = new ArrayList<>();
            source.readList(info.ps, ChannelStatistics.class.getClassLoader());
            return info;
        }

        @Override
        public MediaStatistics[] newArray(int size) {
            return new MediaStatistics[size];
        }
    };

    public List<ChannelStatistics> getTotalList() {
        List<ChannelStatistics> total = new ArrayList<>();

        if(ar != null) {
            total.addAll(ar);
        }

        if(ar != null) {
            total.addAll(as);
        }

        if(ar != null) {
            total.addAll(pr);
        }

        if(ar != null) {
            total.addAll(ps);
        }

        if(ar != null) {
            total.addAll(cr);
        }

        return total;
    }
}