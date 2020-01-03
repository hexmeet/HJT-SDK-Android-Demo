package com.hexmeet.hjt.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IMLoginParams implements Parcelable {
    /**
     * server : 172.24.0.63
     * port : 6060
     * displayName : huanying
     * userId :
     */
    private String server;
    private int port;
    private String displayName;
    private String userId;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public IMLoginParams() {}


    public static Creator<IMLoginParams> CREATOR = new Creator<IMLoginParams>(){

        @Override
        public IMLoginParams createFromParcel(Parcel source) {
            IMLoginParams locations = new IMLoginParams();
            locations.server=source.readString();
            locations.port=source.readInt();
            locations.displayName=source.readString();
            locations.userId=source.readString();
            return null;
        }

        @Override
        public IMLoginParams[] newArray(int size) {
            return new IMLoginParams[size];
        }
    };

    protected IMLoginParams(Parcel in) {
        this.server = in.readString();
        this.port = in.readInt();
        this.displayName = in.readString();
        this.userId = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.server);
        dest.writeInt(this.port);
        dest.writeString(this.displayName);
        dest.writeString(this.userId);
    }

    @Override
    public String toString() {
        return "IMLoginParams{" +
                "server='" + server + '\'' +
                ", port=" + port +
                ", displayName='" + displayName + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
