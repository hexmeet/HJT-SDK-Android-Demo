package com.hexmeet.hjt.cache;

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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEmMessageBodyEvent(EmMessageBody body) {
        if(body != null) {
            LOG.info("add EM message body");
            emMessageBodies.add(body);
            for (SystemStateChangeCallback callback : callbacks) {
                callback.onMessageReciveData(body);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPeopleNumber(PeopleNumberEvent numberEvent) {
        LOG.info("peopleNumber : "+numberEvent.getNumber());
        for (SystemStateChangeCallback callback : callbacks) {
            callback.onGroupAmount(numberEvent.getNumber());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEmLoginSuccessEvent(EmLoginSuccessEvent event) {
        LOG.info("onEmLoginSuccessEvent : "+event.isLoginSucceed());
        HjtApp.getInstance().getAppService().joinGroupChat();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEMGroupMemberInfo(GroupMemberInfo event) {
        if(event!=null){
            EVEngine.ContactInfo info = HjtApp.getInstance().getAppService().getImageUrl(event.getEvUserId());
            if(info!=null){
                IMGroupContactInfo contactInfo = new IMGroupContactInfo();
                contactInfo.setId(String.valueOf(info.id));
                contactInfo.setDisplayName(info.displayName);
                contactInfo.setImageUrl(info.imageUrl);
                contactInfo.setEmUserId(event.emUserId);
                contactInfo.setEvUserId(event.evUserId);
                contactInfos.add(contactInfo);
                for (SystemStateChangeCallback callback : callbacks) {
                    callback.onGroupMemberInfo();
                }
            }
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
}

