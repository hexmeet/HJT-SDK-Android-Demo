package com.hexmeet.hjt;

import org.apache.commons.lang3.StringUtils;

public enum HexMeetTab {
    CONFERENCE(0, "conference"), DIALING(1, "dialing"), ME(2, "me");

    private int index;
    private String tabName;

    HexMeetTab(int index, String name) {
        this.index = index;
        this.tabName = name;
    }

    public int getIndex() {
        return index;
    }

    public String getTabName() {
        return this.tabName;
    }

    public static HexMeetTab fromTabName(String tabName) {
        if (!StringUtils.isEmpty(tabName)) {
            for (HexMeetTab tab : HexMeetTab.values()) {
                if (tab.getTabName().equals(tabName)) {
                    return tab;
                }
            }
        }

        return CONFERENCE;
    }
}
