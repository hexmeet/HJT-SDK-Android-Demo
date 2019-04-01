
package com.hexmeet.hjt.sdk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SvcLayoutInfo implements Parcelable {
    private String speakerName;
    private String layoutMode;
    private ArrayList<String> svcSuit = new ArrayList();
    private ArrayList<Integer> windowIdx = new ArrayList();
    private ArrayList<String> svcDeviceIds = new ArrayList();
    private boolean isOnlyLocal = false;

    public String getSpeakerName() {
        return speakerName;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getLayoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
    }

    public ArrayList<String> getSvcSuit() {
        return svcSuit;
    }

    public void addSuit(String name) {
        svcSuit.add(name);
    }

    public void addDeviceId(String deviceId) {
        svcDeviceIds.add(deviceId);
    }

    public void addWindowIdx(int idx) {
        windowIdx.add(idx);
    }

    public ArrayList<String> getSvcDeviceIds() {
        return svcDeviceIds;
    }

    public ArrayList<Integer> getWindowIdx() {
        return windowIdx;
    }

    public boolean isOnlyLocal() {
        return isOnlyLocal;
    }

    public void setOnlyLocal(boolean onlyLocal) {
        isOnlyLocal = onlyLocal;
    }

    /**
     * @return the creator
     */
    public static Creator<SvcLayoutInfo> getCreator() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(speakerName);
        dest.writeString(layoutMode);
        dest.writeByte((byte) (this.isOnlyLocal ? 1 : 0));
        dest.writeStringList(svcSuit);
        dest.writeStringList(svcDeviceIds);
    }

    public static final Creator<SvcLayoutInfo> CREATOR = new Creator<SvcLayoutInfo>() {

        @Override
        public SvcLayoutInfo createFromParcel(Parcel source) {
            SvcLayoutInfo state = new SvcLayoutInfo();
            state.speakerName = source.readString();
            state.layoutMode = source.readString();
            state.isOnlyLocal = source.readByte() != 0;
            state.svcSuit = source.readArrayList(getClass().getClassLoader());
            state.svcDeviceIds = source.readArrayList(getClass().getClassLoader());
            return state;
        }

        @Override
        public SvcLayoutInfo[] newArray(int size) {
            return new SvcLayoutInfo[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder suits = new StringBuilder();
        for (String s : svcSuit) {
            suits.append("'").append(s).append("'");
        }

        StringBuilder ids = new StringBuilder();
        for (String s : svcDeviceIds) {
            ids.append("'").append(s).append("'");
        }

        StringBuilder windowIdxs = new StringBuilder();
        for (Integer s : windowIdx) {
            windowIdxs.append("'").append(s).append("'");
        }

        return "Mode: ["+layoutMode+"]" + " SpeakerName: ["+speakerName+"] "+"Suits: {"+suits.toString()+"}"+" deviceIds: {"+ids.toString()+"}"+" windowIdx: {"+windowIdxs.toString()+"}";
    }

    public boolean checkSize(int limitSize) {
        if (svcSuit.size() > limitSize) {
            svcSuit = (ArrayList<String>) svcSuit.subList(0, limitSize -1);
        }

        if (svcDeviceIds.size() > limitSize) {
            svcDeviceIds = (ArrayList<String>) svcDeviceIds.subList(0, limitSize -1);
        }

        return (svcSuit.size() == svcDeviceIds.size());
    }
}
