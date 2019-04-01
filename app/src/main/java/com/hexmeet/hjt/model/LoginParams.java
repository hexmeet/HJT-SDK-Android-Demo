package com.hexmeet.hjt.model;


import android.os.Parcel;
import android.os.Parcelable;

public class LoginParams implements Parcelable {

    /**
     * guid : 123456789
     * user_name : scuser1
     * ip_addr : 172.20.0.5
     */

    private String password;
    private String user_name;
    private String serverAddress;
    private String numeric_id;



    public String getNumeric_id() {
        return numeric_id;
    }

    public void setNumeric_id(String numeric_id) {
        this.numeric_id = numeric_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public LoginParams() {}

   public static Creator<LoginParams> CREATOR = new Creator<LoginParams>(){

       @Override
       public LoginParams createFromParcel(Parcel source) {
           LoginParams locations = new LoginParams();
           locations.password=source.readString();
           locations.user_name=source.readString();
           locations.serverAddress=source.readString();
           locations.numeric_id=source.readString();
           return null;
       }

       @Override
       public LoginParams[] newArray(int size) {
           return new LoginParams[size];
       }
   };

    protected LoginParams(Parcel in) {
        this.password = in.readString();
        this.user_name = in.readString();
        this.serverAddress = in.readString();
        this.numeric_id = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.password);
        dest.writeString(this.user_name);
        dest.writeString(this.serverAddress);
        dest.writeString(this.numeric_id);
    }
    @Override
    public String toString() {
        return "Locations{" +
                "password='" + password + '\'' +
                ", user_name='" + user_name + '\'' +
                ", serverAddress='" + serverAddress + '\'' +
                ", numeric_id='" + numeric_id + '\'' +
                '}';
    }
}
