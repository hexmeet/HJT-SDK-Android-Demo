
package com.hexmeet.hjt.sdk;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class MakeCallParam implements Parcelable {
    public String uri;

    public int signalType; //0:sip, 1:h323, 2: SVC

    public int callType; //0: audio call, 1: video call

    public String displayName;

    public String password = "";

    public boolean isP2pCall = false ;
    /**
     * @param uri
     * @param displayName
     */
    public MakeCallParam(String uri, String displayName, int signalType, int callType,boolean isP2pCall) {
        this.uri = uri;
        this.displayName = displayName;
        this.signalType = signalType;
        this.callType = callType;
        this.isP2pCall = isP2pCall;
    }

    public MakeCallParam() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uri);
        dest.writeInt(this.signalType);
        dest.writeInt(this.callType);
        dest.writeString(this.displayName);
        dest.writeString(this.password);
        dest.writeBoolean(this.isP2pCall);
    }

    public static Creator<MakeCallParam> CREATOR = new Creator<MakeCallParam>() {

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public MakeCallParam createFromParcel(Parcel source) {
            MakeCallParam param = new MakeCallParam();
            param.signalType = source .readInt();
            param.uri = source .readString();
            param.displayName = source .readString();
            param.password = source .readString();
            param.callType = source .readInt();
            param.isP2pCall = source.readBoolean();
            return param;
        }

        @Override
        public MakeCallParam[] newArray(int size) {
            return new MakeCallParam[size];
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected MakeCallParam(Parcel in) {
        this.uri = in.readString();
        this.signalType = in.readInt();
        this.callType = in.readInt();
        this.displayName = in.readString();
        this.password = in.readString();
        this.isP2pCall = in.readBoolean();
    }

    @Override
    public String toString() {
        return "Call Param : {Number: ["+uri+"] DisplayName: ["+displayName+"] SignalType: ["+(signalType == 0 ? "Sip" : (signalType == 2 ? "Svc" : "H323"))+"] CallType: ["+(callType == 0 ? "Audio" : "Video")+"] Password:["+password+"] isP2PCall ["+isP2pCall+"]}";
    }
}
