package com.hexmeet.hjt.conf;

public class JsJoinMeeting {

    private String id;
    private String name;
    private String numericId;
    private String password;
    private long startTime;
    private long duration;
    private boolean cameraStatus;
    private boolean microphoneStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumericId() {
        return numericId;
    }

    public void setNumericId(String numericId) {
        this.numericId = numericId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isCameraStatus() {
        return cameraStatus;
    }

    public void setCameraStatus(boolean cameraStatus) {
        this.cameraStatus = cameraStatus;
    }

    public boolean isMicrophoneStatus() {
        return microphoneStatus;
    }

    public void setMicrophoneStatus(boolean microphoneStatus) {
        this.microphoneStatus = microphoneStatus;
    }
}
