package com.hexmeet.hjt.event;

public class EmMessageBody {
    public String groupId;
    public int seq;
    public String content;
    public String from;
    public String time;

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

    public EmMessageBody() {
    }

    public EmMessageBody(String groupId, int seq, String content, String from, String time) {
        this.groupId = groupId;
        this.seq = seq;
        this.content = content;
        this.from = from;
        this.time = time;
    }

    @Override
    public String toString() {
        return "EmMessageBody{" +
                "groupId='" + groupId + '\'' +
                ", seq=" + seq +
                ", content='" + content + '\'' +
                ", from='" + from + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
