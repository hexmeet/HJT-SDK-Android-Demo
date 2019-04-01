package com.hexmeet.hjt.event;

public class FileMessageEvent {
    private boolean success;
    private String filePath;

    public FileMessageEvent(boolean success, String filePath) {
        this.success = success;
        this.filePath = filePath;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
