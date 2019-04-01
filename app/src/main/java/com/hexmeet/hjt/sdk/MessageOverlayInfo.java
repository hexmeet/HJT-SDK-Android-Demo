package com.hexmeet.hjt.sdk;


import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class MessageOverlayInfo implements Parcelable {
    /**
     * on : true
     * content : I have a dream
     * displayRepetitions : 3
     * displaySpeed : 2
     * verticalBorder : 90
     * transparency : 24
     * fontSize : 24
     * foregroundColor : #FFFF00
     * backgroundColor : #3300CC
     */

    private boolean on;
    private String content;
    private int displayRepetitions;
    private int displaySpeed;
    private int verticalBorder;
    private int transparency;
    private int fontSize;
    private String foregroundColor;
    private String backgroundColor;

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getDisplayRepetitions() {
        return displayRepetitions;
    }

    public void setDisplayRepetitions(int displayRepetitions) {
        this.displayRepetitions = displayRepetitions;
    }

    public int getDisplaySpeed() {
        return displaySpeed;
    }

    public void setDisplaySpeed(int displaySpeed) {
        this.displaySpeed = displaySpeed;
    }

    public int getVerticalBorder() {
        return verticalBorder;
    }

    public void setVerticalBorder(int verticalBorder) {
        this.verticalBorder = verticalBorder;
    }

    public int getTransparency() {
        return transparency;
    }

    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(String foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public MessageOverlayInfo() {}

    public MessageOverlayInfo(boolean on, String content, int displayRepetitions, int displaySpeed, int verticalBorder, int transparency, int fontSize, String foregroundColor, String backgroundColor) {
        this.on = on;
        this.content = content;
        this.displayRepetitions = displayRepetitions;
        this.displaySpeed = displaySpeed;
        this.verticalBorder = verticalBorder;
        this.transparency = transparency;
        this.fontSize = fontSize;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public String toString() {
        return "MessageOverlayInfo{" +
                "on=" + on +
                ", content='" + content + '\'' +
                ", displayRepetitions=" + displayRepetitions +
                ", displaySpeed=" + displaySpeed +
                ", verticalBorder=" + verticalBorder +
                ", transparency=" + transparency +
                ", fontSize=" + fontSize +
                ", foregroundColor='" + foregroundColor + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.backgroundColor);
        dest.writeString(this.content);
        dest.writeString(this.foregroundColor);
        dest.writeInt(this.displayRepetitions);
        dest.writeInt(this.displaySpeed);
        dest.writeInt(this.fontSize);
        dest.writeInt(this.on? 1 : 0);
        dest.writeInt(this.transparency);
        dest.writeInt(this.verticalBorder);
    }

    public static final Creator<MessageOverlayInfo> CREATOR = new Creator<MessageOverlayInfo>(){

        @Override
        public MessageOverlayInfo createFromParcel(Parcel source) {
            MessageOverlayInfo info = new MessageOverlayInfo();
            info.backgroundColor = source.readString();
            info.content = source.readString();
            info.foregroundColor = source.readString();
            info.displayRepetitions = source.readInt();
            info.displaySpeed = source.readInt();
            info.fontSize = source.readInt();
            info.on = (source.readInt() == 1);
            info.transparency = source.readInt();
            info.verticalBorder = source.readInt();
            return info;
        }

        @Override
        public MessageOverlayInfo[] newArray(int size) {
            return new MessageOverlayInfo[size];
        }
    };
}
