package com.hexmeet.hjt.event;

public class GroupMemberInfo {
    public String groupId;
    public String name;
    public String emUserId;
    public String evUserId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmUserId() {
        return emUserId;
    }

    public void setEmUserId(String emUserId) {
        this.emUserId = emUserId;
    }

    public String getEvUserId() {
        return evUserId;
    }

    public void setEvUserId(String evUserId) {
        this.evUserId = evUserId;
    }

    @Override
    public String toString() {
        return "GroupMemberInfo{" +
                "groupId='" + groupId + '\'' +
                ", name='" + name + '\'' +
                ", emUserId='" + emUserId + '\'' +
                ", evUserId='" + evUserId + '\'' +
                '}';
    }
}
