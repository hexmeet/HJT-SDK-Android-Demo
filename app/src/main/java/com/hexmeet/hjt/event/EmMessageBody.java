package com.hexmeet.hjt.event;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class EmMessageBody implements Parcelable {
    public String groupId;
    public int seq;
    public String content;
    public String from;
    public String time;
    public boolean isMe;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public EmMessageBody() {
    }

    public EmMessageBody(String groupId, int seq, String content, String from, String time, boolean isMe) {
        this.groupId = groupId;
        this.seq = seq;
        this.content = content;
        this.from = from;
        this.time = time;
        this.isMe = isMe;
    }

    @Override
    public String toString() {
        return "EmMessageBody{" +
                "groupId='" + groupId + '\'' +
                ", seq=" + seq +
                ", content='" + content + '\'' +
                ", from='" + from + '\'' +
                ", time='" + time + '\'' +
                ", isMe=" + isMe +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupId);
        dest.writeInt(seq);
        dest.writeString(content);
        dest.writeString(from);
        dest.writeString(time);
    }
}
