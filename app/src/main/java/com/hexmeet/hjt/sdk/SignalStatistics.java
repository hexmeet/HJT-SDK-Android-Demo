package com.hexmeet.hjt.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class SignalStatistics implements Parcelable {
    public String call_state;
    public String call_type;
    public int call_index;
    public int call_rate;
    public boolean encryption;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(call_state);
        dest.writeString(call_type);
        dest.writeInt(call_index);
        dest.writeInt(call_rate);
        dest.writeByte((byte) (this.encryption ? 1 : 0));
    }
    
    public static final Creator<SignalStatistics> CREATOR = new Creator<SignalStatistics>() {

        @Override
        public SignalStatistics createFromParcel(Parcel source) {
            SignalStatistics info = new SignalStatistics();
            info.call_state = source.readString();
            info.call_type = source.readString();
            info.call_index = source.readInt();
            info.call_rate = source.readInt();
            info.encryption = source.readByte() != 0;
            return info;
        }

        @Override
        public SignalStatistics[] newArray(int size) {
            return new SignalStatistics[size];
        }
    };
}