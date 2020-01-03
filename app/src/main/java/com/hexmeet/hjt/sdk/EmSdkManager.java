package com.hexmeet.hjt.sdk;

import com.hexmeet.hjt.model.IMLoginParams;

import em.common.EMEngine;

public interface EmSdkManager {

    void initEmSDK();

    void releaseEmSdk();

    void emLogin();

    void anonymousLogin(IMLoginParams params);

    void sendMessage(String groupId,String content);

    EMEngine.UserInfo userInfo();

    String getGroupMemberName(String fromId,String groupId);

    void joinGroup(String groupName);

    void logout();
}
