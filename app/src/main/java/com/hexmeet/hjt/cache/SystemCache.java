package com.hexmeet.hjt.cache;

import android.os.SystemClock;
import android.text.TextUtils;

import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.RegisterState;
import com.hexmeet.hjt.dial.RecentPreference;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.ContentEvent;
import com.hexmeet.hjt.event.MicMuteUpdateEvent;
import com.hexmeet.hjt.event.ParticipantsMicMuteEvent;
import com.hexmeet.hjt.event.RecordingEvent;
import com.hexmeet.hjt.event.RefreshLayoutModeEvent;
import com.hexmeet.hjt.event.RemoteMuteEvent;
import com.hexmeet.hjt.login.JoinMeetingParam;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.model.RestLoginResp;
import com.hexmeet.hjt.sdk.MessageOverlayInfo;
import com.hexmeet.hjt.sdk.Peer;
import com.hexmeet.hjt.sdk.SvcLayoutInfo;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemCache {
    private Logger LOG = Logger.getLogger(SystemCache.class);
    private static SystemCache instance;

    private AtomicBoolean sdkReady = new AtomicBoolean(false);

    private boolean networkConnected = true;
    private boolean isWifiConnect = true;

    private RestLoginResp loginResponse;

    private RegisterState registerState = RegisterState.IDLE;
    private boolean isRegServerConnect = true;

    private CallState callState = CallState.IDLE;
    private long startTime = 0L;
    private Peer peer;
    private boolean withContent = false;

    private boolean isUserMuteVideo = false;
    private boolean isUserMuteMic = false;
    private boolean isUserShowLocalCamera = true;
    private boolean isRemoteMuted = false;

    private JoinMeetingParam joinMeetingParam;
    private boolean isAnonymousLogin = false;
    private Map<String, Boolean> participantsMicMuteState = new HashMap<>();

    private boolean layoutModeEnable = true;
    private boolean isRecordingOn = false;

    private boolean isCloudLogin = false;

    private String downloadUserImage;

    private String department;

    private boolean isRecording = false;

    private boolean isInvite = false;

    private String participantNumber;

    private SystemCache() {
        EventBus.getDefault().register(this);
    }


    public static SystemCache getInstance() {
        if(instance == null) {
            instance = new SystemCache();
        }
        return instance;
    }

    public boolean isCloudLogin(){
        return isCloudLogin;
    }
    public void setCloudLogin(boolean cloudLogin) {
        isCloudLogin = cloudLogin;
    }
    public boolean isSdkReady() {
        return sdkReady.get();
    }

    public void setSdkReady(boolean ready) {
        sdkReady.set(ready);
    }

    public boolean isUserMuteVideo() {
        return isUserMuteVideo;
    }

    public void setUserMuteVideo(boolean userMuteVideo) {
        isUserMuteVideo = userMuteVideo;
    }

    public boolean isUserMuteMic() {
        return isUserMuteMic;
    }

    public void setUserMuteMic(boolean userMuteMic) {
        isUserMuteMic = userMuteMic;
    }

    public boolean isNetworkConnected() {
        return networkConnected;
    }

    public void setNetworkConnected(boolean networkConnected) {
        this.networkConnected = networkConnected;
    }

    public boolean isWifiConnect() {
        return isWifiConnect;
    }

    public void setWifiConnect(boolean wifiConnect) {
        isWifiConnect = wifiConnect;
    }

    public RestLoginResp getLoginResponse() {
        return loginResponse;
    }

    public void setLoginResponse(RestLoginResp loginResponse) {
        this.loginResponse = loginResponse;
    }

    public void updateUserDisplayName(String name) {
        if(loginResponse != null) {
            loginResponse.setDisplayName(name);
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void resetAnonymousLoginCache() {
        isAnonymousLogin = false;
        isInvite = false;
        participantNumber=null;
        if(LoginSettings.getInstance().getLoginState(false) == LoginSettings.LOGIN_STATE_IDLE) {
            isUserMuteVideo = false;
            isUserMuteMic = false;
            loginResponse = null;
            isUserShowLocalCamera = true;
        }
    }
    public void resetLoginCache() {
        loginResponse = null;
        isAnonymousLogin = false;
        isUserMuteVideo = false;
        isUserMuteMic = false;
        isUserShowLocalCamera = true;
        downloadUserImage = null;
        department=null;
    }

    public RegisterState getRegisterState() {
        return registerState;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRegisterEvent(RegisterState state) {
        this.registerState = state;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onContentEvent(ContentEvent event) {
        this.withContent = event.withContent();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onCallStateEvent(CallEvent event) {
        switch (event.getCallState()) {
            case IDLE:
                svcLayoutInfo = null;
                overlayInfo = null;
                participantsMicMuteState.clear();
                layoutModeEnable = true;
                isRecordingOn = false;
                repeatCount.set(0);
                withContent = false;
                if(this.callState == CallState.CONNECTED && peer != null) {
                    peer.setEndTime(SystemClock.elapsedRealtime());
                    setStartTime(SystemClock.elapsedRealtime());
                }
                break;
            case CONNECTED:
                this.setStartTime(SystemClock.elapsedRealtime());
                break;
            default:
                break;
        }

        this.callState = event.getCallState();
        if(event.getPeer() != null) {
            LOG.info("SystemCache - Peer got updated, new peer = " + event.getPeer());
            LOG.info("SystemCache - Peer got updated, event call state = " + event.getCallState());

            this.peer = event.getPeer();
            String history = TextUtils.isEmpty(peer.getPassword()) ? peer.getNumber() : (peer.getNumber() + "*" +peer.getPassword());
            RecentPreference.getInstance().updateRecent(history, true);
        }
    }

    public Peer getPeer() {
        return peer;
    }

    public boolean isRegServerConnect() {
        return isRegServerConnect;
    }

    public void setRegServerConnect(boolean regServerConnect) {
        isRegServerConnect = regServerConnect;
    }

    private SvcLayoutInfo svcLayoutInfo;
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSvcLayoutChangedEvent(SvcLayoutInfo info) {
        svcLayoutInfo = info;
    }

    public SvcLayoutInfo getSvcLayoutInfo() {
        return svcLayoutInfo;
    }

    private MessageOverlayInfo overlayInfo;
    private AtomicInteger repeatCount = new AtomicInteger(0);

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageOverlayEvent(MessageOverlayInfo event) {
        overlayInfo = event;
        repeatCount.set(0);
    }

    public MessageOverlayInfo getOverlayMessage() {
        return overlayInfo;
    }

    public int getIncrementRepeatCount() {
        return repeatCount.incrementAndGet();
    }

    public void clearOverlayMessage() {
        overlayInfo = null;
        repeatCount.set(0);
    }

    public boolean withContent() {
        return withContent;
    }

    public void setWithContent(boolean withContent) {
        this.withContent = withContent;
    }

    public boolean isCalling() {
        LOG.info("isCalling ? ： " + callState);
        return callState != CallState.IDLE;
    }
    public boolean isConnecting() {
        return callState == CallState.CONNECTING && peer != null;
    }
    public boolean isUserShowLocalCamera() {
        return isUserShowLocalCamera;
    }

    public void setUserShowLocalCamera(boolean show) {
        isUserShowLocalCamera = show;
    }

    public JoinMeetingParam getJoinMeetingParam() {
        if(joinMeetingParam == null) {
            joinMeetingParam = new JoinMeetingParam();
        }
        return joinMeetingParam;
    }

    public void setJoinMeetingParam(JoinMeetingParam param) {
        this.joinMeetingParam = param;
    }

    public boolean isAnonymousMakeCall() {
        return isAnonymousLogin;
    }

    public void setAnonymousMakeCall(boolean anonymousLogin) {
        isAnonymousLogin = anonymousLogin;
    }


    public boolean isInviteMakeCall() {
        return isInvite;
    }

    public void setInviteMakeCall(boolean anonymousInvite) {
        isInvite = anonymousInvite;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMruMuteEvent(RemoteMuteEvent event) {
        LOG.info("Indication : sys "+ event.isMuteFromMru());
        isRemoteMuted = event.isMuteFromMru();
    }

    public boolean isRemoteMuted() {
        return isRemoteMuted;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onParticipantsMicMuteEvent(ParticipantsMicMuteEvent event) {
        if(event != null ) {
            participantsMicMuteState.put(event.getParticipants(), event.isMute());
            EventBus.getDefault().post(new MicMuteUpdateEvent(event.getParticipants()));
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRecordingEvent(RecordingEvent event) {
        if(event != null) {
            isRecordingOn = (event == RecordingEvent.ON);
        }
    }

    public boolean isRemoteDeviceMicMuted(String deviceId) {
        if(participantsMicMuteState.containsKey(deviceId)) {
            return participantsMicMuteState.get(deviceId);
        }
        return false;
    }

    public boolean isLayoutModeEnable() {
        return layoutModeEnable;
    }

    public void setLayoutModeEnable(boolean layoutModeEnable) {
        this.layoutModeEnable = layoutModeEnable;
        if(!layoutModeEnable && !AppSettings.getInstance().isSpeakerMode()) {
            EventBus.getDefault().post(new RefreshLayoutModeEvent(true));
        }
    }

    public boolean isRecordingOn() {
        return isRecordingOn;
    }
    public String getDownloadUserImage() {
        return downloadUserImage;
    }
    public void setDownloadUserImage(String downloadUserImage) {
        this.downloadUserImage = downloadUserImage;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isRecording(){
        return isRecording;
    }
    public void setRecording(boolean recording) {
        isRecording = recording;
    }

  //  participantNumber

    public String getParticipant() {
        return participantNumber;
    }

    public void setParticipant(String participantNumber) {
        this.participantNumber = participantNumber;
    }

    public String getToken(){
        return loginResponse == null ? null : loginResponse.getToken();
    }

    public String getDoradoVersion() {
        return loginResponse == null ? null : loginResponse.getDoradoVersion();
    }
}
