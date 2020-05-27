package com.hexmeet.hjt.event;

public class ChatAddrGrpidEvent {
    public String address;
    public String groupId;

    public String getAddress() {
        return address;
    }

    public String getGroupId() {
        return groupId;
    }

    public ChatAddrGrpidEvent(String address, String groupId) {
        this.address = address;
        this.groupId = groupId;
    }
}
