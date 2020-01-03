package com.hexmeet.hjt.model;

public class IMGroupContactInfo {
    public String id;
    public String displayName;
    public String imageUrl;
    public String emUserId;
    public String evUserId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
        return "IMGroupContactInfo{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", emUserId='" + emUserId + '\'' +
                ", evUserId='" + evUserId + '\'' +
                '}';
    }
}
