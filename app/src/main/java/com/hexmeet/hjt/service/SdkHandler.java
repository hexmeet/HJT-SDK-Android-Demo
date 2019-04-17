package com.hexmeet.hjt.service;

import android.os.Build;
import android.os.HandlerThread;
import android.os.Message;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.AvatarUploadEvent;
import com.hexmeet.hjt.login.LoginService;
import com.hexmeet.hjt.model.LoginParams;
import com.hexmeet.hjt.sdk.ChannelStatList;
import com.hexmeet.hjt.sdk.MakeCallParam;
import com.hexmeet.hjt.sdk.SdkManager;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

class SdkHandler extends BaseSafelyHandler<SdkManager> {
    private Logger LOG = Logger.getLogger(SdkHandler.class);

    final static int HANDLER_SDK_INIT = 10001;
    final static int HANDLER_SDK_DRECTION = 10002;
    final static int HANDLER_SDK_RELEASE = 10003;

    final static int HANDLER_SDK_ANSWER_CALL = 10010;
    final static int HANDLER_SDK_MAKE_CALL = 10011;
    final static int HANDLER_SDK_DROP_CALL = 10012;
    final static int HANDLER_SDK_RELOAD_CAMERA = 10013;

    final static int HANDLER_SDK_MIC_MUTE = 10020;
    final static int HANDLER_SDK_SWITCH_CAMERA= 10021;
    final static int HANDLER_SDK_SET_LAYOUT_MODE = 10022;
    final static int HANDLER_SDK_GET_STATISTICS = 10023;
    final static int HANDLER_SDK_TOGGLE_VIDEO_MUTE = 10024;
    final static int HANDLER_SDK_SEND_CONTENT = 10025;
    final static int HANDLER_SDK_SAVE_USER_IMAGE = 10026;
    final static int HANDLER_SDK_USER_IMAGE = 10027;
    final static int HANDLER_SDK_USER_SPEAKER = 10028;

    final static int HANDLER_LOGIN = 20001;
    final static int HANDLER_LOGOUT = 20002;
    final static int HANDLER_HAND_UP = 20003;
    final static int HANDLER_AUTO_LOGIN = 20004;

    final static int HANDLER_ANONYMOUS_MAKECALL = 20005;
    /* TODO: this may not only happen during the call setup case, need to check */

    final static int HANDLER_UPLOAD_AVATAR = 20006;
    final static int HANDLER_USER_RENAME = 20007;
    final static int HANDLER_USER_PASSWORD = 20008;
    final static int HANDLER_USER_LOGPATH = 20009;
    final static int HANDLER_USER_MESSAGE = 200010;
    final static int HANDLER_NETWORK_STATUS = 20011;

    SdkHandler(HandlerThread thread, SdkManager ref) {
        super(thread.getLooper(), ref);
    }

    @Override
    public void handleMessage(SdkManager ref,Message msg) {
        try {
            LOG.info("Handle SDK message: (msg.what) = " + msg.what);
            switch (msg.what) {
                case HANDLER_SDK_INIT:
                    LOG.info(" service create , " + " ==== Phone information dump ====");
                    dumpDeviceInformation();
                    ref.initSDK();
                    break;
                case HANDLER_SDK_DRECTION:
                    ref.setDeviceRotation(msg.arg1);
                    break;
                case HANDLER_SDK_RELEASE:
                    ref.release();
                    break;
                case HANDLER_SDK_RELOAD_CAMERA:
                    ref.reLoadCamera();
                    break;
                case HANDLER_SDK_MIC_MUTE:
                    ref.setMicMute(msg.arg1 == 1);
                    break;
                case HANDLER_SDK_ANSWER_CALL:
                    MakeCallParam answerParam = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                    if(answerParam != null) {
                        ref.answerCall(answerParam);
                    }
                    break;
                case HANDLER_SDK_DROP_CALL:
                    ref.dropCall();
                    break;
                case HANDLER_SDK_MAKE_CALL:
                    MakeCallParam callParam = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                    if(callParam != null) {
                        ref.makeCall(callParam);
                    }
                    break;
                case HANDLER_SDK_SWITCH_CAMERA:
                    ref.switchCamera();
                    break;
                case HANDLER_SDK_SET_LAYOUT_MODE:
                    ref.setSvcLayoutMode(msg.arg1);
                    break;

                case HANDLER_SDK_SEND_CONTENT:
                    break;

                case HANDLER_SDK_GET_STATISTICS:
                    ChannelStatList channelStatList = ref.getMediaStatics();
                    if(channelStatList != null) {
                        EventBus.getDefault().post(channelStatList);
                    }
                    sendEmptyMessageDelayed(HANDLER_SDK_GET_STATISTICS, 3000);
                    break;
                case HANDLER_SDK_TOGGLE_VIDEO_MUTE:
                    ref.enableVideo(msg.arg1 == 1);
                    break;
                case HANDLER_SDK_SAVE_USER_IMAGE:
                    String path = (String) msg.obj;
                    ref.updateVideoUserImage(new File(path));
                    break;
                case HANDLER_LOGIN:
                    LoginParams params = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                    ref.login(params,msg.arg1 == 1,msg.obj == null ? null : (String) msg.obj);
                    break;
                case HANDLER_LOGOUT:
                    ref.logout();
                    break;
                case HANDLER_HAND_UP:
                    ref.handUp();
                    break;
                case HANDLER_AUTO_LOGIN:
                    if(SystemCache.getInstance().isAnonymousMakeCall()) {
                        LoginService.getInstance().anonymousMakeCall();
                    } else {
                        LoginService.getInstance().autoLogin();
                    }
                    break;
                case HANDLER_ANONYMOUS_MAKECALL:
                    if(SystemCache.getInstance().isAnonymousMakeCall()) {
                        ref.anonymousMakeCall();
                    }
                    break;

                case HANDLER_UPLOAD_AVATAR:
                    String paths = (String) msg.obj;
                    if(paths != null) {
                        File savedFile = new File(paths);
                        if(savedFile.exists()) {
                            ref.uploadAvatar(savedFile);
                        } else {
                            EventBus.getDefault().post(new AvatarUploadEvent(false, "save bitmap to sdcard failed"));
                        }
                    } else {
                        EventBus.getDefault().post(new AvatarUploadEvent(false, "No bitmap or Recycled in Extras data"));
                    }
                    break;

                case HANDLER_USER_RENAME:
                    ref.rename((String) msg.obj);
                    break;
                case HANDLER_USER_PASSWORD:
                    String oldPassword = msg.getData().getString("oldPwd");
                    String newPassword = msg.getData().getString("newPwd");
                    ref.updatePassword(oldPassword,newPassword);
                    break;
                case HANDLER_USER_LOGPATH:
                    ref.getObtainLogPath();
                    break;
                case HANDLER_USER_MESSAGE:
                    ref.getUserInfo();
                    break;
                case HANDLER_NETWORK_STATUS:
                    ref.networkQuality();
                    break;
                case HANDLER_SDK_USER_IMAGE:
                    ref.onPhoneStateChange(msg.arg1 == 1);
                    break;
                case HANDLER_SDK_USER_SPEAKER:
                    ref.onEnableSpeaker(msg.arg1 == 1);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LOG.error("SdkHandler: "+e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        LOG.error("dumpDeviceInformation , " + sb.toString());
    }

}
