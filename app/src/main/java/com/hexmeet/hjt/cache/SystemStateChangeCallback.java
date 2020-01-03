package com.hexmeet.hjt.cache;

import com.hexmeet.hjt.event.EmMessageBody;

public interface SystemStateChangeCallback {
    void onMessageReciveData(EmMessageBody messageBody);
    void onGroupAmount(String number);
    void onGroupMemberInfo();
}
