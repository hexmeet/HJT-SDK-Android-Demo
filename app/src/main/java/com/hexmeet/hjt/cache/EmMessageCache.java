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

import em.common.EMEngine;
import ev.common.EVEngine;

public class EmMessageCache {
    private Logger LOG = Logger.getLogger(EmMessageCache.class);

    private static EmMessageCache instance;

    private ArrayList<EmMessageBody> emMessageBodies = new ArrayList<EmMessageBody>();//获取IM消息条目
    private List<SystemStateChangeCallback> callbacks = new ArrayList<>();//接口回调更新UI
    private ArrayList<IMGroupContactInfo> contactInfos = new ArrayList<IMGroupContactInfo>();// 获取im userName、emUserId

    boolean isInitialed = false;
    private String groupId;
    boolean isIMAddress = false;
    boolean isIMSuccess = false;
    private String iMAddress;

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


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEmMessageBodyEvent(EMEngine.EMMessageBody body) {
        if(body != null) {
           if(emMessageBodies.size()!=0){
               boolean isExist = false;
               for(int i=0;i<emMessageBodies.size();i++){
                   if(body.seq  == emMessageBodies.get(i).getSeq()){
                       LOG.info("body.getSeq()  " + body.seq+",old seq : "+emMessageBodies.get(i).getSeq());
                       isExist = true;
                       break;
                     }
                }
               if(!isExist){
                   LOG.info("body.getSeq() " + body.seq);
                   updateImMessage(body);
               }
           }else {
                LOG.info("add message listview ");
               updateImMessage(body);
            }
        }
    }

    private void updateImMessage(EMEngine.EMMessageBody body){
        EmMessageBody  emMessageBody = new EmMessageBody();
        emMessageBody.setGroupId(body.groupId);
        emMessageBody.setSeq(body.seq);
        emMessageBody.setContent(body.content);
        emMessageBody.setFrom(body.from);
        emMessageBody.setTime(body.time);
        emMessageBody.setMe(false);
        emMessageBodies.add(emMessageBody);
        addMessage(emMessageBody);
    }



    private void addMessage(EmMessageBody body) {
        for (SystemStateChangeCallback callback : callbacks) {
            callback.onMessageReciveData(body);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEmLoginSuccessEvent(EmLoginSuccessEvent event) {
        LOG.info("onEmLoginSuccessEvent : "+event.isLoginSucceed());
        setIMSuccess(event.isLoginSucceed());
        HjtApp.getInstance().getAppService().joinGroupChat();//获取会议号
        EMEngine.UserInfo info = HjtApp.getInstance().getAppService().getImUserInfo();
        if(info!=null && info.userid!=null){
            HjtApp.getInstance().getAppService().setImUserId(info.userid);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEMGroupMemberInfo(GroupMemberInfo event) {
        if (event != null) {
           /*  EVEngine.ContactInfo info = HjtApp.getInstance().getAppService().getImageUrl(event.getEvUserId());
               if(info!=null){
                    LOG.info("info : "+info.toString());
                    contactInfo.setId(String.valueOf(info.id));
                    contactInfo.setDisplayName(info.displayName);
                    contactInfo.setImageUrl(info.imageUrl);
                }else {
                    String groupMemberName = HjtApp.getInstance().getAppService().getGroupMemberName(event.getEmUserId(), getGroupId());
                    LOG.info("groupMemberName : " + groupMemberName);
                    contactInfo.setDisplayName(groupMemberName);
                }*/

                if(contactInfos.size()!=0){
                   boolean isExist = false;
                    for(int i=0;i<contactInfos.size();i++){
                        if(event.getEmUserId().equals(contactInfos.get(i).getEmUserId()) ){
                            if(!event.getName().equals(contactInfos.get(i).getDisplayName())){
                                LOG.info("onEMGroupMemberInfo : Group UserName :" + event.getName() + ",old name :" + contactInfos.get(i).getDisplayName());
                                contactInfos.remove(i);
                                updateGroupUserName(event);
                                isExist = true;
                                break;
                            }
                            isExist = true;
                        }
                    }
                   if(!isExist){
                       LOG.info("onEMGroupMemberInfo : no isExist ");
                       updateGroupUserName(event);
                   }
        } else {
            updateGroupUserName(event);
        }

    }

    }

    private void updateGroupUserName(GroupMemberInfo event){
        IMGroupContactInfo contactInfo = new IMGroupContactInfo();
        contactInfo.setDisplayName(event.getName());
        contactInfo.setEmUserId(event.getEmUserId());
        contactInfo.setEvUserId(event.getEvUserId());
        contactInfos.add(contactInfo);
        for (SystemStateChangeCallback callback : callbacks) {
            callback.onGroupMemberInfo();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEmError(CallState state) {
       LOG.info("onEmError() : " + state.toString());
       if(state==CallState.EMERROR && getiMAddress()!=null){
           HjtApp.getInstance().getAppService().anonymousLoginIM(getiMAddress());
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
        iMAddress = null;
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

    public String getiMAddress() {
        return iMAddress;
    }

    public void setiMAddress(String iMAddress) {
        this.iMAddress = iMAddress;
    }
}

