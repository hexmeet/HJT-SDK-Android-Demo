package com.hexmeet.hjt.groupchat;


public abstract class BaseAction {

    private int iconResId;

    private int titleId;

    private Container container;


    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    protected BaseAction(int iconResId, int titleId) {
        this.iconResId = iconResId;
        this.titleId = titleId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public abstract void onClick();

}
