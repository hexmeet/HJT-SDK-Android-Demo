package com.hexmeet.hjt.cache;

import com.hexmeet.hjt.event.EmMessageBody;

public class SystemStateAdapter implements SystemStateChangeCallback{

    @Override
    public void onMessageReciveData(EmMessageBody messageBody) {}

    @Override
    public void onGroupAmount(String number) {}

    @Override
    public void onGroupMemberInfo() {}
}
