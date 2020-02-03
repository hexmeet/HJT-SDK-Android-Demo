package com.hexmeet.hjt.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.RegisterState;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.AvatarUploadEvent;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.ContentEvent;
import com.hexmeet.hjt.event.FileMessageEvent;
import com.hexmeet.hjt.event.LiveEvent;
import com.hexmeet.hjt.event.LogPathEvent;
import com.hexmeet.hjt.event.LoginResultEvent;
import com.hexmeet.hjt.event.LoginRetryEvent;
import com.hexmeet.hjt.event.MicMuteChangeEvent;
import com.hexmeet.hjt.event.MuteSpeaking;
import com.hexmeet.hjt.event.NetworkEvent;
import com.hexmeet.hjt.event.NetworkStatusEvent;
import com.hexmeet.hjt.event.ParticipantsMicMuteEvent;
import com.hexmeet.hjt.event.PeopleNumberEvent;
import com.hexmeet.hjt.event.RecordingEvent;
import com.hexmeet.hjt.event.RemoteMuteEvent;
import com.hexmeet.hjt.event.RemoteNameUpdateEvent;
import com.hexmeet.hjt.event.RenameEvent;
import com.hexmeet.hjt.event.SvcSpeakerEvent;
import com.hexmeet.hjt.event.UserInfoEvent;
import com.hexmeet.hjt.event.UserPasswordEvent;
import com.hexmeet.hjt.login.JoinMeetingParam;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.model.FeatureSupport;
import com.hexmeet.hjt.model.LoginParams;
import com.hexmeet.hjt.model.RestLoginResp;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ev.common.EVCommon;
import ev.common.EVEngine;
import ev.common.EVEventListener;
import ev.common.EVFactory;

import static ev.common.EVEngine.CallInfo;
import static ev.common.EVEngine.ContentInfo;
import static ev.common.EVEngine.EVError;
import static ev.common.EVEngine.LayoutIndication;
import static ev.common.EVEngine.LayoutMode;
import static ev.common.EVEngine.LayoutPage;
import static ev.common.EVEngine.LayoutRequest;
import static ev.common.EVEngine.LayoutSpeakerIndication;
import static ev.common.EVEngine.LayoutType;
import static ev.common.EVEngine.MessageOverlay;
import static ev.common.EVEngine.RecordingInfo;
import static ev.common.EVEngine.Site;
import static ev.common.EVEngine.StreamStats;
import static ev.common.EVEngine.StreamType;
import static ev.common.EVEngine.UserInfo;
import static ev.common.EVEngine.Warning;


public class SdkManagerImpl implements SdkManager {
    private Logger LOG = Logger.getLogger(SdkManagerImpl.class);
    public EVEngine engine;
    public static final int CODE_SUCCESS = 0;
    static final  int LOGIN_ERROR_1101 = 1101;
    static final  int LOGIN_ERROR_10009 = 10009;
    static final  int LOGIN_ERROR_1112 = 1112;
    static final  int LOGIN_ERROR_8 = 8;
    public static final int LOGIN_ERROR_9 = 9;
    public static final int SWITCH_AUDIO_MODE = 0;
    public static final int SWITCH_VIDEO_MODE = 3;

    @Override
    public void initSDK() {
        LOG.info("initSDK");
        Context appContext = HjtApp.getInstance().getContext();

        engine = EVFactory.createEngine();
        LOG.info("init engine ->" + engine.toString());
        CopyAssets.getInstance().createAndStart(appContext);
        String path = appContext.getFilesDir().getAbsolutePath();
        engine.setLog("EasyVideo", path, "evsdk", 1024 * 1024 * 20);
        engine.enableLog(true);
        engine.setRootCA(path);
        engine.initialize(appContext,path, "config");
        engine.setUserAgent("HexMeet", Utils.getVersion());
        engine.setMaxRecvVideo(AppCons.MAX_RECEIVE_STREAM);
        engine.setBandwidth(AppCons.DEFAULT_BITRATE);
        //engine.enablePreviewFrameCb(StreamType.Video, true);
        //engine.enablePreviewFrameCb(StreamType.Content, true);
        engine.addEventListener(new EVListenr());
        SystemCache.getInstance().setSdkReady(true);
    }



    @Override
    public void dropCall() {
        LOG.info("dropCall invoked");
        engine.leaveConference();
    }

    @Override
    public void release() {
        LOG.info("Sdk release");
        dropCall();
        engine.removeEventListener(new EVListenr());
        SystemCache.getInstance().setSdkReady(false);
    }

    @Override
    public void login(LoginParams params, boolean https, String port) {
        engine.enableSecure(https);
        String password = engine.encryptPassword(params.getPassword());
        int loginPort = 0;
        if (!TextUtils.isEmpty(port)) {
            loginPort = Integer.parseInt(port);
        }
        LOG.info("login : " + params.getServerAddress()+", loginPort : "+loginPort+", User_name: "+ params.getUser_name());
       engine.loginWithLocation(params.getServerAddress(), loginPort, params.getUser_name(), password);
    }

    @Override
    public void anonymousMakeCall() {
        JoinMeetingParam params = SystemCache.getInstance().getJoinMeetingParam();
        engine.enableSecure(params.isUseHttps());
        int loginPort = 0;
        if (!TextUtils.isEmpty(params.getPort())) {
            loginPort = Integer.parseInt(params.getPort());
        }

        isFrontCamera();//判断是否是视频输入

        LOG.info("anonymousLogin : "+params.getServer() +","+ loginPort+","+ params.getConferenceNumber()+","+params.getDisplayName());
        engine.joinConferenceWithLocation(params.getServer(),loginPort,params.getConferenceNumber(),params.getDisplayName(),params.getPassword());

        Peer peer = new Peer(Peer.DIRECT_OUT);
        peer.setNumber(params.getConferenceNumber());
        peer.setName(params.getConferenceNumber());
        peer.setPassword(params.getPassword());
        peer.setVideoCall(true);
        CallEvent event = new CallEvent(CallState.CONNECTING);
        event.setPeer(peer);
        EventBus.getDefault().post(event);

        updateVideoUserImage(null);

    }

    @Override
    public void setDeviceRotation(int deviceRotation) {
        LOG.info("setDeviceRotation: ["+deviceRotation+"]");
        engine.setDeviceRotation(deviceRotation);
    }

    @Override
    public void getObtainLogPath() {
        String path = engine.compressLog();
        EventBus.getDefault().post(new LogPathEvent(path));
    }

    @Override
    public void getUserInfo() {
        UserInfo user = engine.getUserInfo();
        if(user!=null){
        LOG.info("getUserInfoList : "+user.toString());
        getUserInfoList(user,true);
        }
    }

    @Override
    public void zoomVideoByStreamType(StreamType video, float factor, float cx, float cy) {
        engine.zoomRemoteWindow(video,factor, cx, cy);
    }

    @Override
    public void networkQuality() {
        float quality = engine.getNetworkQuality();
        LOG.info(" networkQuality : "+quality);
       // EventBus.getDefault().post(new NetworkStatusEvent(quality));
    }

    @Override
    public void onPhoneStateChange(boolean isPhone) {
        if(isPhone){
            engine.setUserImage(CopyAssets.getInstance().mBackgroundCallingFile,
                    CopyAssets.getInstance().mUserFile);
        }else {
            engine.setUserImage(CopyAssets.getInstance().mBackgroundFile,
                    CopyAssets.getInstance().mUserFile);
        }

    }

    @Override
    public void onEnableSpeaker(boolean isSpeaker) {
        LOG.info(" isSpeaker : "+isSpeaker);
        engine.enableSpeaker(isSpeaker);
    }

    @Override
    public boolean isCalling() {
        //+ (engine == null), new Exception()
       LOG.info("isCalling engine = null? ");
        if(engine!=null && engine.getCallInfo()!=null){
            return true;
        }else {
            return  false;
        }

    }

    @Override
    public void isHardDecoding(boolean hardDecoding) {
        LOG.info("isHardDecoding : "+hardDecoding);
        engine.enableHardDecoding(hardDecoding);
    }

    @Override
    public void isFrontCamera() {
        EVEngine.Device current_device = engine.getDevice(EVEngine.DeviceType.VideoCapture);
        if(current_device==null || current_device.name == null || !current_device.name.endsWith("f")){
            LOG.info("current device is not front camera. device: " + current_device);
            List<EVEngine.Device> devices = engine.getDevices(EVEngine.DeviceType.VideoCapture);
            if(devices != null && devices.size() > 0) {
                for(int i = 0; i < devices.size(); i++) {
                    EVEngine.Device device = devices.get(i);
                    if(device != null && device.name != null && device.name.endsWith("f")) {
                        engine.setDevice(EVEngine.DeviceType.VideoCapture, device.id);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setVideoActive(boolean mode) {
        if(mode){
            engine.setVideoActive(SWITCH_VIDEO_MODE);
        }else {
            engine.setVideoActive(SWITCH_AUDIO_MODE);
        }
        LOG.info("setVideoActive "+mode+",videoActive : "+engine.videoActive());
    }

    @Override
    public boolean micEnabled() {
        return engine.micEnabled();
    }

    @Override
    public void refuseP2PMeeting(String number) {
        LOG.info("refuseP2PMeeting : "+number);
        engine.declineIncommingCall(number);
    }

    @Override
    public void setConfDisplayName(String displayName) {
        LOG.info("setConfDisplayName : "+displayName);
        engine.setConfDisplayName(displayName);
    }

    @Override
    public String getDisplayName() {
        LOG.info("getDisplayName : "+engine.getDisplayName());
        return engine.getDisplayName();
    }

    @Override
    public String getIMAddress() {
        LOG.info("getImAddress : "+engine.getImAddress());
        return engine.getImAddress();
    }

    @Override
    public String getIMGroupId() {
        return engine.getImGroupId();
    }

    @Override
    public EVEngine.ContactInfo getIMContactInfo(String userId) {
        LOG.info("getIMContactInfo userId ： "+userId);
        EVEngine.ContactInfo contactInfo = engine.getContactInfo(userId, 1);
        return contactInfo;
    }

    @SuppressLint("StringFormatInvalid")
    public static void handlerError(int errorCode, String error, ArrayList<String> time) {
        boolean needRetry = true;

        if (error != null) {
            if (error.contains("java.net.SocketTimeoutException") || error.contains("connect timed out")) {
                errorCode = LoginResultEvent.LOGIN_WRONG_NET;
                error = HjtApp.getInstance().getString(R.string.server_unavailable);

            } else if (error.contains("Invalid user name or password") || error.contains("Did not find this username")) {
                error = HjtApp.getInstance().getString(R.string.invalid_username_or_password);
                errorCode = LoginResultEvent.LOGIN_WRONG_PASSWORD;
                needRetry = false;
            } else if (error.contains("Your account has been temporarily locked, please try again later or contact the administrator")) {
                error = HjtApp.getInstance().getString(R.string.disable_account);
                errorCode = LoginResultEvent.LOGIN_WRONG_PASSWORD;
                needRetry = false;
            }else if (errorCode == LOGIN_ERROR_1101) {
                String frequency = time.get(0);
                errorCode=LoginResultEvent.LOGIN_WRONG_PASSWORD_TIME;
                error = HjtApp.getInstance().getString(R.string.locking_password,frequency);
                needRetry = false;
            }else if (errorCode == LOGIN_ERROR_10009) {
                errorCode=LoginResultEvent.LOGIN_WRONG_INVALID_NAME;
                error = HjtApp.getInstance().getString(R.string.invalid_username_or_password);
                needRetry = false;
            } else if (errorCode == LOGIN_ERROR_8 || errorCode == LOGIN_ERROR_9) {
                errorCode=LoginResultEvent.LOGIN_WRONG_LOCATION_SERVER;
                error = HjtApp.getInstance().getString(R.string.cannot_connect_location_server);
                needRetry = false;
            }else if (error.contains("400 Bad Request")) {
                errorCode = LoginResultEvent.LOGIN_WRONG_NET;
                error = HjtApp.getInstance().getString(R.string.server_port_unavailable);
            }else if (errorCode == LOGIN_ERROR_1112) {
                errorCode=LoginResultEvent.LOGIN_WRONG_NO_PERMISSION;
                error = HjtApp.getInstance().getString(R.string.login_error_code_1112);
                needRetry = false;
            } else {
                error = HjtApp.getInstance().getString(R.string.login_again) + " [" + errorCode + "]";
            }
        } else {
            error =  HjtApp.getInstance().getString(R.string.login_again) + " [" + errorCode + "]";
        }

        EventBus.getDefault().post(new LoginResultEvent(errorCode, error));
        if (needRetry) {
            EventBus.getDefault().post(new LoginRetryEvent());
        }
    }

    @Override
    public void logout() {
        LOG.info("logout");
        engine.logout();
        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, false);
        SystemCache.getInstance().resetLoginCache();

        NetworkUtil.shutdown();
        updateVideoUserImage(null);
    }

    @Override
    public void makeCall(MakeCallParam param) {
        LOG.info(" makeCall " + param.uri+" : "+param.displayName+" : "+param.password);
        isFrontCamera();
        int code = engine.joinConference(param.uri, SystemCache.getInstance().getLoginResponse().displayName, param.password);
        LOG.info(" makeCall code "+ code);

        if(code!=0){
            CallEvent events = new CallEvent(CallState.IDLE);
            events.setEndReason(ResourceUtils.getInstance().getCallFailedReason(code));
            EventBus.getDefault().post(events);
            engine.leaveConference();
            return;
        }
        
            Peer peer = new Peer(Peer.DIRECT_OUT);
            peer.setNumber(param.uri);
            peer.setName(param.displayName);
            peer.setPassword(param.password);
            peer.setVideoCall(param.callType == 1);
            peer.setP2P(param.isP2pCall);
            CallEvent event = new CallEvent(CallState.CONNECTING);
            event.setPeer(peer);
            EventBus.getDefault().post(event);


    }

    @Override
    public void p2pMakeCall(MakeCallParam param) {
        setVideoActive(true);
        isFrontCamera();
        LOG.info("p2pMakeCall userid :" + param.uri+",name : "+param.displayName+" ,password: "+param.password);
        engine.joinConference(param.uri, SystemCache.getInstance().getLoginResponse().displayName,param.password,EVEngine.CallType.SvcCallP2P);
    }

    @Override
    public void answerCall(MakeCallParam param) {
        LOG.info(" answerCall " + param.uri+" : "+param.displayName+" : "+param.password);
        setVideoActive(true);
        isFrontCamera();
        engine.joinConference(param.uri,SystemCache.getInstance().getLoginResponse().displayName,param.password);

    }

    @Override
    public void enableVideo(boolean enable) {
        LOG.info(" enableVideo " + enable +",cameraEnabled : "+engine.cameraEnabled());
        if(enable ^ engine.cameraEnabled()){
            engine.enableCamera(enable);
        }else {
            LOG.error("no enableVideo instance!!!");
        }

    }

    @Override
    public void handUp() {
        boolean remoteMute = engine.remoteMuted();
        LOG.info(" remoteMute ： " + remoteMute);
        if(remoteMute){
            engine.requestRemoteUnmute(true);
        }else {
            engine.requestRemoteUnmute(false);
        }

    }

    @Override
    public void setMicMute(boolean mute) {
        LOG.info(" setMicMute " + mute +" micEnabled : "+engine.micEnabled());
        if(mute ^ !engine.micEnabled()){
            engine.enableMic(!mute);
        }else {
            LOG.error("no MicMute instance!!!");
        }
    }


    @Override
    public void reLoadCamera() {
        LOG.info("reLoadCamera()");
        engine.reloadVideoDevices();
    }

    @Override
    public void setSvcLayoutMode(int svcLayoutMode) {
       LOG.info("setSvcLayoutMode: " + (svcLayoutMode == 0 ? "Auto" : (svcLayoutMode == 1 ? "Gallery" : "Speaker")));
       LayoutType type = (svcLayoutMode == 0) ? LayoutType.typeAuto : ((svcLayoutMode == 2) ? LayoutType.type1 : LayoutType.type4);
       LayoutRequest request = new LayoutRequest(LayoutMode.fromInt(svcLayoutMode),type, LayoutPage.typeCurrent, EVCommon.VideoSize.VIDEO_SIZE_UNKNOWN,null);
       engine.setLayout(request);
    }

    @Override
    public void switchCamera() {
        engine.switchCamera();
    }

    @Override
    public void setLocalSurface(SurfaceView view) {
        LOG.info("setLocalSurface()");
        engine.setLocalVideoWindow(view);
    }

    @Override
    public void setRemoteSurface(Object[] surfaces) {
        LOG.info("setRemoteSurface()");
        engine.setRemoteVideoWindow(Arrays.asList(surfaces));
    }

    @Override
    public void setPreSurface(SurfaceView view) {
        LOG.info("setPreSurface");
        engine.setPreviewVideoWindow(view);
    }

    @Override
    public void setContentSurface(SurfaceView view) {
        if (view == null) {
            LOG.info("SDK setContentSurfaceView: null");
        } else if (view.getHolder() != null && view.getHolder().getSurface() != null) {
            LOG.info("SDK setContentSurfaceView: " + view.getHolder().getSurface().toString());
        } else {
            LOG.info("SDK setContentSurfaceView: Holder or Surface null");
        }
       engine.setRemoteContentWindow(view);
    }

    @Override
    public ChannelStatList getMediaStatics() {

        ChannelStatList channelStatList = new ChannelStatList();
        MediaStatistics media = new MediaStatistics();
        SignalStatistics signalStat = new SignalStatistics();
        signalStat.call_type = "SVC";


        List<ChannelStatistics> arxList = new ArrayList<>();
        List<ChannelStatistics> atxList = new ArrayList<>();
        List<ChannelStatistics> pvrxList = new ArrayList<>();
        List<ChannelStatistics> pvtxList = new ArrayList<>();
        List<ChannelStatistics> cvrxList = new ArrayList<>();


        ArrayList<StreamStats> stats = engine.getStats();
        for (StreamStats streamStats :stats) {
            signalStat.encryption = streamStats.isEncrypted;
            String streamType = streamStats.type.toString();
            LOG.info(" streamStats.type : "+streamStats.toString());
            if(streamType.equals("Audio")){

                LOG.info(" streamStats.dir : "+streamStats.dir.value());
                int value = streamStats.dir.value();
                if(value ==1){

                    ChannelStatistics arx = new ChannelStatistics();
                    arx.codec = streamStats != null ? streamStats.payloadType : "-";
                    arx.rtp_actualBitRate = (int) streamStats.realBandwidth ;
                    arx.rtp_settingBitRate =  streamStats != null ? (int) streamStats.negoBandwidth / 1000 : 0;
                    arx.packetLostRate = (int)streamStats.packetLossRate;
                    arx.packetLost =(int) streamStats.cumPacketLoss;
                    arx.encrypted = streamStats.isEncrypted;
                    arx.pipeName = "AR";
                    arxList.add(arx);

                }else {

                    ChannelStatistics atx = new ChannelStatistics();
                    atx.codec = streamStats != null ? streamStats.payloadType : "-";
                    atx.rtp_actualBitRate = (int) streamStats.realBandwidth ;
                    atx.rtp_settingBitRate =  streamStats != null ? (int) streamStats.negoBandwidth / 1000 : 0;
                    atx.packetLostRate = (int)streamStats.packetLossRate;
                    atx.packetLost =(int) streamStats.cumPacketLoss;
                    atx.encrypted = streamStats.isEncrypted;
                    atx.pipeName = "AS";
                    atxList.add(atx);

                }
            }else if(streamType.equals("Video")){
                LOG.info(" streamStats.dir : "+streamStats.dir.value());
                String codec = "";
                String payloadType = streamStats.payloadType;
                String[] payload = payloadType.split(" ");
                for(int i = 0; i < payload.length; i++){
                   codec = payload[0];
                }
                int value = streamStats.dir.value();
                if(value ==1){
                    ChannelStatistics pvrx = new ChannelStatistics();
                    pvrx.codec = streamStats != null ? codec : "-";
                    pvrx.rtp_actualBitRate = (int) streamStats.realBandwidth ;
                    pvrx.rtp_settingBitRate = streamStats != null ? (int) streamStats.negoBandwidth / 1000 : 0;
                    pvrx.packetLost = (int) streamStats.cumPacketLoss;
                    pvrx.packetLostRate =(int) streamStats.packetLossRate;
                    pvrx.frameRate = (int)streamStats.fps;
                    pvrx.resolution = streamStats.resolution.width +"x" +streamStats.resolution.height;
                    pvrx.encrypted = streamStats.isEncrypted;
                    pvrx.pipeName = streamStats.name;
                    pvrxList.add(pvrx);

                }else {
                    ChannelStatistics pvtx = new ChannelStatistics();
                    pvtx.codec = streamStats != null ? codec : "-";
                    pvtx.rtp_actualBitRate = (int) streamStats.realBandwidth ;
                    pvtx.rtp_settingBitRate = streamStats != null ? (int) streamStats.negoBandwidth / 1000 : 0;
                    pvtx.packetLost = (int) streamStats.cumPacketLoss;
                    pvtx.packetLostRate =(int) streamStats.packetLossRate;
                    pvtx.frameRate = (int)streamStats.fps;
                    pvtx.resolution = streamStats.resolution.width +"x" +streamStats.resolution.height;
                    pvtx.encrypted = streamStats.isEncrypted;
                    pvtx.pipeName = "PS-" + streamStats.ssrc;
                    pvtxList.add(pvtx);
                }


            }else if(streamType.equals("Content")){
                ChannelStatistics cvrx = new ChannelStatistics();
                cvrx.pipeName = "CR";
                cvrx.codec = streamStats != null ? streamStats.payloadType : "-";
                cvrx.rtp_actualBitRate =  (int) streamStats.realBandwidth ;
                cvrx.rtp_settingBitRate = streamStats != null ? (int) streamStats.negoBandwidth / 1000 : 0;
                cvrx.packetLost = (int) streamStats.cumPacketLoss;
                cvrx.packetLostRate =(int) streamStats.packetLossRate;
                cvrx.frameRate = (int)streamStats.fps;
                cvrx.resolution = streamStats.resolution.width +"x" +streamStats.resolution.height;
                cvrx.encrypted = streamStats.isEncrypted;
                cvrxList.add(cvrx);
            }



        }

        media.ar = arxList;
        media.as = atxList;
        media.cr = cvrxList;
        media.pr = pvrxList;
        media.ps = pvtxList;

        channelStatList.media_statistics = media;
        channelStatList.signal_statistics = signalStat;


        return channelStatList;
    }

    @Override
    public void uploadAvatar(final File file) {
        engine.uploadUserImage(file.getPath());
    }

    @Override
    public void downloadAvatar() {
         LOG.info("downloadAvatar  path: "+HjtApp.getInstance().getFilesDir() + "avatar_scale" + ".jpg");
         engine.downloadUserImage(HjtApp.getInstance().getFilesDir() + "/avatar_scale" + ".jpg");
    }

    @Override
    public void updateVideoUserImage(File imageFile) {
       if (imageFile != null) {
            try {
                Utils.copyFile(imageFile, CopyAssets.getInstance().mUserFile);
                engine.setUserImage(CopyAssets.getInstance().mBackgroundFile,CopyAssets.getInstance().mUserFile);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            imageFile = new File(CopyAssets.getInstance().mUserFile);
            try {
                if(imageFile.exists()) {
                    imageFile.delete();
                }
                CopyAssets.getInstance().copyFromPackage(R.raw.user, imageFile.getName());
                engine.setUserImage(CopyAssets.getInstance().mBackgroundFile,CopyAssets.getInstance().mUserFile);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void rename(final String name) {
        int code = engine.changeDisplayName(name);
        if (code == CODE_SUCCESS) {
          //  SystemCache.getInstance().updateUserDisplayName(name);
            EventBus.getDefault().post(new RenameEvent(true, name));
        } else {
            EventBus.getDefault().post(new RenameEvent(false, "fail"));
        }
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        String oldPass = engine.encryptPassword(oldPassword);
        String newPass = engine.encryptPassword(newPassword);
        int code = engine.changePassword(oldPass, newPass);
        if(code==CODE_SUCCESS){
            if(LoginSettings.getInstance().isCloudLoginSuccess()) {
                LoginSettings.getInstance().setCloudPassword(newPassword);
            } else {
                LoginSettings.getInstance().setPrivatePassword(newPassword);
            }
            EventBus.getDefault().post(new UserPasswordEvent(true, "success"));
        }else {
            EventBus.getDefault().post(new UserPasswordEvent(false, "fail"));
        }
        LOG.info("changePassword  : " + code );
    }



    public static void getUserInfoList(UserInfo info, boolean isObtain){
        EVEngine.EVFeatureSupport feature = info.featureSupport;
        RestLoginResp restLoginResp = new RestLoginResp();
        FeatureSupport featureSupport = new FeatureSupport();
        restLoginResp.setUserId(info.userId);
        restLoginResp.setUsername(info.username);
        restLoginResp.setDisplayName(info.displayName);
        restLoginResp.setOrg(info.org);
        restLoginResp.setEmail(info.email);
        restLoginResp.setCellphone(info.cellphone);
        restLoginResp.setTelephone(info.telephone);
        restLoginResp.setDept(info.dept);
        restLoginResp.setEverChangedPasswd(info.everChangedPasswd);
        restLoginResp.setCustomizedH5UrlPrefix(info.customizedH5UrlPrefix);
        restLoginResp.setToken(info.token);
        restLoginResp.setDoradoVersion(info.doradoVersion);
        restLoginResp.setDeviceId(info.deviceId);
        featureSupport.setContactWebPage(feature.contactWebPage);
        featureSupport.setChatInConference(feature.chatInConference);
        featureSupport.setP2pCall(feature.p2pCall);
        featureSupport.setSwitchingToAudioConference(feature.switchingToAudioConference);
        featureSupport.setSitenameIsChangeable(feature.sitenameIsChangeable);
        restLoginResp.setFeatureSupport(featureSupport);
        SystemCache.getInstance().setLoginResponse(restLoginResp);
        if(isObtain){
            EventBus.getDefault().post(new UserInfoEvent(info.username,info.displayName,info.org,info.email,info.cellphone,info.telephone,info.dept));
        }

    }

    class EVListenr extends EVEventListener {
        public static final int CALL_ERROR_2015 = 2015;
        @Override
        public void onError(EVError err) {
            LOG.info("CallBack onError code：" + err.toString());
            if( err.type !=null ){
                if(err.type.toString()==ErrorType.EVErrorTypeLocate || err.type.toString()==ErrorType.EVErrorTypeServer){
                    if(SystemCache.getInstance().isAnonymousMakeCall()){
                        CallEvent event = new CallEvent(CallState.IDLE);
                        event.setEndReason(ResourceUtils.getInstance().getCallFailedReason(err.code));
                        EventBus.getDefault().post(event);
                        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, true);
                        EventBus.getDefault().post(new LoginResultEvent(LoginResultEvent.LOGIN_ANONYMOUS_FAILED, "No callBack in response", true));
                    }else {
                        SdkManagerImpl.handlerError(err.code, err.msg ,err.arg);
                    }

                }else if(err.type.toString()== ErrorType.EVErrorTypeSdk){
                    if(SystemCache.getInstance().isAnonymousMakeCall()){
                        CallEvent event = new CallEvent(CallState.IDLE);
                        event.setEndReason(ResourceUtils.getInstance().getCallFailedReason(err.code));
                        EventBus.getDefault().post(event);
                    }else {
                        if(err.code== ResourceUtils.CALL_ERROR_SDK_10){
                            CallEvent event = new CallEvent(CallState.IDLE);
                            event.setEndReason(ResourceUtils.getInstance().getCallFailedReason(ResourceUtils.CALL_ERROR_SDK_10));
                            EventBus.getDefault().post(event);
                        }else if(err.code == ResourceUtils.CALL_ERROR_9 && err.action.equals("downloadUserImage")){
                            return;
                        }else {
                            SdkManagerImpl.handlerError(err.code, err.msg ,err.arg);
                        }

                    }
                }/*else if(err.type.toString()== ErrorType.EVErrorTypeCall){
                    //TODO
                }else if(err.type.toString()== ErrorType.EVErrorTypeUnknown){
                    //TODO
                }*/

            }

        }

        @Override
        public void onLoginSucceed(UserInfo user) {
                LOG.info("CallBack onLoginSucceed: " + user.toString());
                SdkManagerImpl.getUserInfoList(user,false);
                if(SystemCache.getInstance().isAnonymousMakeCall()){
                    LOG.info("CallBack isCloud : "+SystemCache.getInstance().getJoinMeetingParam().isCloud());
                    LoginSettings.getInstance().setLoginState(SystemCache.getInstance().getJoinMeetingParam().isCloud() ? LoginSettings.LOGIN_CLOUD_SUCCESS : LoginSettings.LOGIN_PRIVATE_SUCCESS, true);
                    EventBus.getDefault().post(new LoginResultEvent(LoginResultEvent.LOGIN_SUCCESS, "success", true));
                    updateVideoUserImage(null);
                }else {
                    boolean isCloudLogin = SystemCache.getInstance().isCloudLogin();
                    LOG.info("CallBack isCloudLogin : "+isCloudLogin);
                    LoginSettings.getInstance().setLoginState(isCloudLogin ? LoginSettings.LOGIN_CLOUD_SUCCESS : LoginSettings.LOGIN_PRIVATE_SUCCESS, false);
                    EventBus.getDefault().post(new LoginResultEvent(LoginResultEvent.LOGIN_SUCCESS, "success"));
                    downloadAvatar();
                }

        }

        @Override
        public void onJoinConferenceIndication(CallInfo info) {//邀请入会
            LOG.info("onJoinConferenceIndication   "+info.toString());
            Peer peer = new Peer(Peer.DIRECT_IN);
            peer.setNumber(info.conferenceNumber);
            peer.setPassword(info.password);
            peer.setFrom(info.peer);
            peer.setVideoCall(true);
            if(info.type==EVEngine.CallType.SvcCallP2P){
                peer.setName(info.peer);
                peer.setCalled(true);
                peer.setP2P(true);
            } else {
                peer.setP2P(false);
                peer.setName(info.conferenceNumber);
            }
            if(info.action==EVEngine.CallAction.SvcIncomingCallCancel){
                CallEvent event = new CallEvent(CallState.IDLE);
                event.setEndReason(ResourceUtils.getInstance().getCallFailedReason(ResourceUtils.CALL_ERROR_SDK_101));
                EventBus.getDefault().post(event);
                return;
            }
            CallEvent event = new CallEvent(CallState.RING);
            event.setPeer(peer);
            EventBus.getDefault().post(event);

        }

        @Override
        public void onDownloadUserImageComplete(String path) {
            File file = new File(path);
            if(file.exists()){
                LOG.info("CallBack Download : "+path);
                SystemCache.getInstance().setDownloadUserImage(path);
                updateVideoUserImage(file);
                EventBus.getDefault().post(new FileMessageEvent(true,path));
            }else {
                updateVideoUserImage(null);
                EventBus.getDefault().post(new FileMessageEvent(false,path));
            }
        }

        @Override
        public void onUploadUserImageComplete(String path) {
            LOG.info("CallBack UploadUser : "+path);
            File file = new File(path);
            if(file.exists()){
                LOG.info("CallBack UploadUser : "+file);
                SystemCache.getInstance().setDownloadUserImage(path);
                AvatarUploadEvent event = new AvatarUploadEvent(true, "success");
                event.setAvatarFilePath(path);
                EventBus.getDefault().post(event);
            } else {
                EventBus.getDefault().post(new AvatarUploadEvent(false, "no"));
            }

        }

        @Override
        public void onCallConnected(CallInfo info) {
            LOG.info("CallBack CallConnected=="+info.toString());
       if(info!=null && info.type==EVEngine.CallType.SvcCallP2P){
            CallEvent event = new CallEvent(CallState.PEERCONNECTED);
            EventBus.getDefault().post(event);
        }else {
            CallEvent event = new CallEvent(CallState.CONNECTED);
            EventBus.getDefault().post(event);
        }

        }

        @Override
        public void onCallEnd(CallInfo info) {
            LOG.info("CallBack onCallEnd: "+info.toString());
            if (info.err.code == CALL_ERROR_2015) {
                LOG.info("CallBack. CallEnd Password empty or wrong");
                CallEvent event = new CallEvent(CallState.AUTHORIZATION);
                EventBus.getDefault().post(event);
            } else {
                LOG.info("CallBack. CallEnd ");
                CallEvent event = new CallEvent(CallState.IDLE);
                event.setEndReason(ResourceUtils.getInstance().getCallFailedReason(info.err.code));
                EventBus.getDefault().post(event);
            }
        }

        @Override
        public void onLayoutIndication(LayoutIndication layout) {//布局内容发生变化
            LOG.info("CallBack. onLayoutIndication toString "+layout.toString());
            if(layout != null){
                SvcLayoutInfo info = new SvcLayoutInfo();
                info.setLayoutMode(layout.mode.toString());//画廊模式
                info.setSpeakerName(layout.speakerName);
                List<Site> sites = layout.sites;
                SystemCache.getInstance().setLayoutModeEnable(layout.modeSettable);

                if(sites != null){
                    for (Site sit : sites){
                        LOG.info(" CallBack  onLayoutIndication: " + sit.toString());
                        info.addSuit(sit.name);
                        info.addWindowIdx((Integer) sit.window);
                        info.addDeviceId(String.valueOf(sit.deviceId));//gradle 分配号 服务器分配给我们的
                        if(sit.isLocal){
                            if(SystemCache.getInstance().isRemoteMuted() ^ sit.remoteMuted) {
                                LOG.info(" CallBack  onLayoutIndication: isRemoteMuted " + SystemCache.getInstance().isRemoteMuted()+", sit.remoteMuted : "+ sit.remoteMuted);
                                EventBus.getDefault().post(new RemoteMuteEvent(sit.remoteMuted));
                            }

                        } else {
                            EventBus.getDefault().post(new ParticipantsMicMuteEvent(sit.micMuted,String.valueOf(sit.deviceId)));
                        }
                    }
                    if(sites.size() == 1 && sites.get(0).isLocal == true){
                        info.setOnlyLocal(true);
                    }

                }
                LOG.info("CallBack onLayoutIndication svcLayout: " + info.toString());
                EventBus.getDefault().post(info);
            }
        }

        @Override
        public void onLayoutSiteIndication(Site site) {//状态发生变化
            LOG.info("CallBack onLayoutSiteIndication: " + site.toString());
           if(site.isLocal){
               if(SystemCache.getInstance().isRemoteMuted() ^ site.remoteMuted) {
                   LOG.info("isMuteFromMru  sdk ");
                   EventBus.getDefault().post(new RemoteMuteEvent(site.remoteMuted));
               }
               EventBus.getDefault().post(new RemoteNameUpdateEvent(site.window,site.name,String.valueOf(site.deviceId),true));
            } else {
               EventBus.getDefault().post(new ParticipantsMicMuteEvent(site.micMuted,String.valueOf(site.deviceId)));
               EventBus.getDefault().post(new RemoteNameUpdateEvent(site.window,site.name,String.valueOf(site.deviceId),false));
            }

        }

        @Override
        public void onLayoutSpeakerIndication(LayoutSpeakerIndication speaker) {//发言者信息
            LOG.info("onLayoutSpeakerIndication: speakerIndex :" + speaker.speakerIndex +" speakerName : "+speaker.speakerName);
            if (speaker != null) {
                SvcSpeakerEvent event = new SvcSpeakerEvent(speaker.speakerIndex, speaker.speakerName);
                EventBus.getDefault().post(event);
            }
        }

        @Override
        public void onContent(ContentInfo info) {//双流
            LOG.info("onContent: enabled:" + info.enabled +" type : "+info.type +" dir : "+info.dir);
            SystemCache.getInstance().setWithContent(info.enabled);
            EventBus.getDefault().post(new ContentEvent(info.enabled));
        }

        @Override
        public void onMessageOverlay(MessageOverlay overlayInfo) { //字幕
            LOG.info("CallBack MessageOverlay: " + overlayInfo.toString());
            if(overlayInfo != null){
                EventBus.getDefault().post(new MessageOverlayInfo(overlayInfo.enable,overlayInfo.content,overlayInfo.displayRepetitions,overlayInfo.displaySpeed,overlayInfo.verticalBorder
                ,overlayInfo.transparency,overlayInfo.fontSize,overlayInfo.foregroundColor,overlayInfo.backgroundColor));
            }
        }

        @Override
        public void onMuteSpeakingDetected() {//提示打开声音
            LOG.info("CallBack onMuteSpeakingDetected isUserMuteMic  : YES ? "+engine.micEnabled()+", isCloseTips "+AppSettings.getInstance().isCloseTips());
            if(!engine.micEnabled() && !AppSettings.getInstance().isCloseTips()){
                EventBus.getDefault().post(new MuteSpeaking(true));
            }
        }

        @Override
        public void onRecordingIndication(RecordingInfo state) {//录制false / 直播true
            if (state != null && state.states != null) {
                LOG.info("CallBack onRecordingIndication: " +  state.live+",state "+state.states);
                SystemCache.getInstance().setRecording(state.live);
                EventBus.getDefault().post(new LiveEvent(state.live));
                if(state.states ==state.states.On) {
                    EventBus.getDefault().post(RecordingEvent.ON);
                } else {
                    EventBus.getDefault().post(RecordingEvent.OFF);
                }
            }
        }

        @Override
        public void onRegister(boolean registered) {
            LOG.info("onRegister: " + registered);
            if (registered) {
                EventBus.getDefault().post(RegisterState.SUCCESS);
            } else {
                EventBus.getDefault().post( RegisterState.IDLE);
            }
        }
        @Override
        public void onWarnMessage(Warning warnMessage) {//会议中网络和带宽相关通知提示
            LOG.info("onWarn: "+warnMessage.toString()+", isCloseTips "+AppSettings.getInstance().isCloseTips());

            if(NetworkEvent.EV_WARN_UNMUTE_AUDIO_INDICATION.equals(warnMessage.code.toString())|| NetworkEvent.EV_WARN_UNMUTE_AUDIO_NOT_ALLOWED.equals(warnMessage.code.toString())){
                EventBus.getDefault().post(new NetworkEvent(warnMessage.code.toString()));
            } else if(!AppSettings.getInstance().isCloseTips()){
                EventBus.getDefault().post(new NetworkEvent(warnMessage.code.toString()));
            }
        }

        @Override
        public void onNetworkQuality(float qualityRating) {
            LOG.info("onNetworkQuality: "+qualityRating);
            EventBus.getDefault().post(new NetworkStatusEvent(qualityRating));
        }

        @Override
        public void onParticipant(final int number) {
            LOG.info("onParticipant: "+number);
            SystemCache.getInstance().setParticipant(number+"");
            EventBus.getDefault().post(new PeopleNumberEvent(number+""));
         }

        @Override
        public void onCallPeerConnected(CallInfo info) {//p2p
            LOG.info("onCallPeerConnected: "+info.toString());
            SystemCache.getInstance().getPeer().setNumber(info.conferenceNumber);
            CallEvent event = new CallEvent(CallState.CONNECTED);
            EventBus.getDefault().post(event);

        }

        public void onVideoPreviewFrame(byte[] frame) {
             LOG.info("onVideoPreviewFrame: "+ frame.length);
         }


        public void onContentPreviewFrame(byte[] frame) {
            LOG.info("onVideoPreviewFrame: "+ frame.length);
        }

        @Override
        public void onMicMutedShow(int mic_muted) {
            LOG.info("Local mute state  :"+mic_muted);//1 静音 ，0 非静音
            if(mic_muted==1){
                EventBus.getDefault().post(new MicMuteChangeEvent(true));
            }else {
                EventBus.getDefault().post(new MicMuteChangeEvent(false));
            }
        }

        @Override
        public void onPeerImageUrl(String imageUrl) {//获取p2p头像
            LOG.info("onPeerImageUrl()");
            if(imageUrl!=null && !TextUtils.isEmpty(imageUrl)){
                SystemCache.getInstance().getPeer().setImageUrl(imageUrl);
                EventBus.getDefault().post(new FileMessageEvent(true,imageUrl));
            }
        }
    }
}
