package com.hexmeet.hjt.cache;

import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.event.EmLoginSuccessEvent;
import com.hexmeet.hjt.event.EmMessageBody;
import com.hexmeet.hjt.event.GroupMemberInfo;
import com.hexmeet.hjt.event.PeopleNumberEvent;
import com.hexmeet.hjt.model.IMGroupContactInfo;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ev.common.EVEngine;

public class EmMessageCache {
    private Logger LOG = Logger.getLogger(EmMessageCache.class);

    private static EmMessageCache instance;

    private ArrayList<EmMessageBody> emMessageBodies = new ArrayList<EmMessageBody>();;
    private List<SystemStateChangeCallback> callbacks = new ArrayList<>();
    private ArrayList<IMGroupContactInfo> contactInfos = new ArrayList<IMGroupContactInfo>();

    boolean isInitialed = false;
    private String groupId;
    boolean isIMAddress = false;
    boolean isIMSuccess = false;

    public static EmMessageCache getInstance() {
        if(instance == null) {
            instance = new EmMessageCache();
        }
        return instance;
    }

    private EmMessageCache() {
        EventBus.getDefault().register(this);
    }

    public void initCache() {
        if(!isInitialed) {
            isInitialed = true;
        }
    }

    public List<EmMessageBody> getMessageBody() {
        return emMessageBodies;
    }

    public void addMessageBody(EmMessageBody messageBody) {
        LOG.info("addMessageBody()");
        if(emMessageBodies ==null){
            emMessageBodies = new ArrayList<EmMessageBody>();
        }
        emMessageBodies.add(messageBody);
    }

    public void unRegisterSystemCallBack(SystemStateChangeCallback callback){
        callbacks.remove(callback);
    }
    public void registerSystemCallBack(SystemStateChangeCallback callback) {
        if(!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEmMessageBodyEvent(EmMessageBody body) {
        if(body != null) {
           if(emMessageBodies.size()!=0){
               boolean contains = emMessageBodies.contains(body);
               LOG.info("Is there a duplicate message ： "+contains);
               if(!contains){
                   emMessageBodies.add(body);
                   addMessage(body);
               }
           }else {
                LOG.info("add message listview ");
                emMessageBodies.add(body);
                addMessage(body);
            }
        }
    }

    private void addMessage(EmMessageBody body) {
        for (SystemStateChangeCallback callback : callbacks) {
            callback.onMessageReciveData(body);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEmLoginSuccessEvent(EmLoginSuccessEvent event) {
        LOG.info("onEmLoginSuccessEvent : "+event.isLoginSucceed());
        setIMSuccess(event.isLoginSucceed());
        HjtApp.getInstance().getAppService().joinGroupChat();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEMGroupMemberInfo(GroupMemberInfo event) {
        if(event!=null){
            EVEngine.ContactInfo info = HjtApp.getInstance().getAppService().getImageUrl(event.getEvUserId());
                IMGroupContactInfo contactInfo = new IMGroupContactInfo();
                if(info!=null){
                    LOG.info("info : "+info.toString());
                    contactInfo.setId(String.valueOf(info.id));
                    contactInfo.setDisplayName(info.displayName);
                    contactInfo.setImageUrl(info.imageUrl);
                }else {
                    String groupMemberName = HjtApp.getInstance().getAppService().getGroupMemberName(event.getEmUserId(), getGroupId());
                    LOG.info("groupMemberName : " + groupMemberName);
                    contactInfo.setDisplayName(groupMemberName);
                }
                contactInfo.setEmUserId(event.getEmUserId());
                contactInfo.setEvUserId(event.getEvUserId());
                contactInfos.add(contactInfo);
                for (SystemStateChangeCallback callback : callbacks) {
                    callback.onGroupMemberInfo();
                }

        }

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEmError(CallState state) {
       LOG.info("onEmError() : " + state.toString());
       if(state==CallState.EMERROR){
           HjtApp.getInstance().getAppService().anonymousLoginIM();
       }
    }


    public List<IMGroupContactInfo> getContactInfo() {
        return contactInfos;
    }

    public void resetIMCache(){
        LOG.info("resetIMCache");
        emMessageBodies.clear();
        groupId = null;
        contactInfos.clear();
        isIMSuccess= false;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isIMAddress() {
        return isIMAddress;
    }

    public void setIMAddress(boolean IMAddress) {
        isIMAddress = IMAddress;
    }

    public boolean isIMSuccess() {
        return isIMSuccess;
    }

    public void setIMSuccess(boolean IMSuccess) {
        isIMSuccess = IMSuccess;
    }
}

