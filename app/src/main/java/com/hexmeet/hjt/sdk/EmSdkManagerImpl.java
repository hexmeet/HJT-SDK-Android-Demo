package com.hexmeet.hjt.sdk;

import android.content.Context;
import android.text.TextUtils;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.cache.EmMessageCache;
import com.hexmeet.hjt.event.GroupMemberInfo;
import com.hexmeet.hjt.event.EmLoginSuccessEvent;
import com.hexmeet.hjt.event.EmMessageBody;
import com.hexmeet.hjt.model.IMLoginParams;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import em.common.EMEngine;
import em.common.EMEventListener;
import em.common.EMFactory;


public class EmSdkManagerImpl implements EmSdkManager{
    private Logger LOG = Logger.getLogger(EmSdkManagerImpl.class);
    private EMEngine engine;
    private EMListenr emListenr;

    @Override
    public void initEmSDK() {
        LOG.info("initEmSDK");
        Context appContext = HjtApp.getInstance().getContext();
        String path = appContext.getFilesDir().getAbsolutePath();
        engine = EMFactory.createEngine();
        engine.setLog("EasyMessage", path, "emsdk", 1024 * 1024 * 20);
        engine.enableLog(true);
        engine.initialize(path, "em_config");
        engine.setRootCA(path);
        emListenr = new EMListenr();
        engine.addIMEventListener(emListenr);
        EmMessageCache.getInstance().initCache();
    }

    @Override
    public void releaseEmSdk() {
        LOG.info("releaseEmSdk()");
       engine.removeIMEventListener(emListenr);
    }

    @Override
    public void emLogin() {
        LOG.info("Login()");
        engine.login("172.24.0.63",6060,"basic","bob","bob123");
    }

    @Override
    public void anonymousLogin(IMLoginParams params) {
        LOG.info("anonymousLogin() :"+params.toString());
        engine.anonymousLogin(params.getServer(),params.getPort(),params.getDisplayName(),params.getUserId());
    }

    @Override
    public void sendMessage(String groupId, String content) {
        LOG.info("sendMessage : "+groupId+",content : "+content);
        engine.sendMessage(groupId,content);
    }

    @Override
    public EMEngine.UserInfo userInfo() {
        EMEngine.UserInfo userInfo = engine.getUserInfo();
        return  userInfo;
    }

    @Override
    public String getGroupMemberName(String fromId, String groupId) {
        LOG.info("fromId : "+fromId+",groupId : "+groupId);
        if(!TextUtils.isEmpty(fromId) && !TextUtils.isEmpty(groupId)){
            String groupMemberName = engine.getGroupMemberName(fromId, groupId);
            LOG.info("groupMemberName : "+groupMemberName);
            return groupMemberName;
        }
      return null;
    }

    @Override
    public void joinGroup(String groupName) {
        LOG.info("joinGroup() : "+groupName );
        engine.joinNewGroup(groupName);
    }

    @Override
    public void logout() {
        LOG.info("logout()");
        engine.logout();
    }


    class EMListenr extends EMEventListener {

        @Override
        public void onError(EMEngine.EMError err) {
            LOG.info("onError : "+err.toString());
        }

        @Override
        public void onAllMessagesReceived(EMEngine.EMMessagesReceived msgReceived) {
            LOG.info("onAllMessagesReceived : "+msgReceived.toString());
        }

        @Override
        public void onLoginSucceed() {
            LOG.info("onIMLoginSucceed  ");
            EventBus.getDefault().post(new EmLoginSuccessEvent(true));
        }

        @Override
        public void onMessageReciveData(EMEngine.EMMessageBody messageBody) {
            LOG.info("onMessageReciveData : "+messageBody.toString());
                EmMessageBody  emMessageBody = new EmMessageBody();
                emMessageBody.setGroupId(messageBody.groupId);
                emMessageBody.setSeq(messageBody.seq);
                emMessageBody.setContent(messageBody.content);
                emMessageBody.setFrom(messageBody.from);
                emMessageBody.setTime(messageBody.time);
                EventBus.getDefault().post(emMessageBody);

        }

        @Override
        public void onMessageSendSucceed(EMEngine.EMMessageSendBlock sendBlock) {
            LOG.info("onMessageReciveData : "+sendBlock.toString());
        }

        @Override
        public void onGroupMemberInfo(EMEngine.EMGroupMemberInfo info) {
            LOG.info("onGroupMemberInfo : "+info.toString());
            GroupMemberInfo memberInfo = new GroupMemberInfo();
            memberInfo.setName(info.name);
            memberInfo.setGroupId(info.groupId);
            memberInfo.setEmUserId(info.emUserId);
            memberInfo.setEvUserId(info.evUserId);
            EventBus.getDefault().post(memberInfo);
        }
    }

}
