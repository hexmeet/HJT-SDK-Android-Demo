package com.hexmeet.hjt.service;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.RegisterState;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.LoginResultEvent;
import com.hexmeet.hjt.event.LoginRetryEvent;
import com.hexmeet.hjt.model.LoginParams;
import com.hexmeet.hjt.sdk.CopyAssets;
import com.hexmeet.hjt.sdk.MakeCallParam;
import com.hexmeet.hjt.sdk.SdkManager;
import com.hexmeet.hjt.sdk.SdkManagerImpl;
import com.hexmeet.hjt.utils.NetworkUtil;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ev.common.EVEngine;

public class AppService extends Service {
    private Logger LOG = Logger.getLogger(AppService.class);

    private AppServiceBinder mBinder = new AppServiceBinder();
    private HeadsetPlugReceiver headsetPlugReceiver = new HeadsetPlugReceiver();
    private SdkHandler mSdkHandler;
    private SdkManager sdkManager;
    private boolean userInLogin = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class AppServiceBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        LOG.info("->AppService onCreate<-");
        EventBus.getDefault().register(this);

        sdkManager = new SdkManagerImpl();

        HandlerThread sdkThread = new HandlerThread("SdkHandlerThread");
        sdkThread.start();
        mSdkHandler = new SdkHandler(sdkThread, sdkManager);
        mSdkHandler.sendEmptyMessageDelayed(SdkHandler.HANDLER_SDK_INIT, 200);
        registerNetworkReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.info("->AppService onDestroy<-");
        unregisterReceiver(mNetworkReceiver);
        EventBus.getDefault().unregister(this);
        releaseSdk();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        if(event.getCallState() == CallState.CONNECTED) {
            initAudioMode(true);
        }

        if(event.getCallState() == CallState.RING && !HjtApp.getInstance().isCalling()) {
            Intent intent = new Intent();
            intent.setAction(AppCons.SYSTEM_CALLING_ACTION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            getApplicationContext().startActivity(intent);
        }

        if(event.getCallState() == CallState.IDLE){
            cancelFloatIndicator();
            uninitAudioMode(true);
        }
    }


    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            LOG.info("NetworkStateService - onReceive intent: " + intent);
            if (NetworkUtil.isNetConnected(context)) {
                LOG.info("NetworkStateService - onReceive Net connected");
                boolean isWifiConnect = NetworkUtil.isWifiConnected(context);
                if(!SystemCache.getInstance().isNetworkConnected() || (isWifiConnect ^ SystemCache.getInstance().isWifiConnect())) {
                    LOG.info("NetworkStateService - onReceive Net re-connected, so re-login");
                    onRegisterEvent(RegisterState.FAILED);
                }
                SystemCache.getInstance().setNetworkConnected(true);
                SystemCache.getInstance().setWifiConnect(isWifiConnect);

            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    // network onChanged
                    String name = info.getTypeName();
                    LOG.info("NetworkStateService - onReceive Net changed, name: " + name);
                } else {
                    // network onDisconnected
                    LOG.info("NetworkStateService - onReceive Net disconnected");
                    SystemCache.getInstance().setNetworkConnected(false);
                    SystemCache.getInstance().setWifiConnect(false);
                }
            }
        }
    };

    private void registerNetworkReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, mFilter);
    }

    public void releaseSdk() {
        sdkManager.release();
    }

    public void loginInThread(LoginParams params, boolean https, String port){
        Message msg = Message.obtain();
        msg.what = SdkHandler.HANDLER_LOGIN;
        msg.arg1 = https ? 1 : 0;
        msg.obj = port;
        Bundle data = msg.getData();
        data.putParcelable(AppCons.BundleKeys.EXTRA_DATA, params);
        msg.setData(data);
        mSdkHandler.sendMessage(msg);
    }

    public void cameraDirection(int driection) {
        Log.i("camear Direction : ",""+driection);
        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_DRECTION;
        message.arg1=driection;
        mSdkHandler.sendMessage(message);
    }

    public void anonymousMakeCall(){
        Message msg = Message.obtain();
        msg.what = SdkHandler.HANDLER_ANONYMOUS_MAKECALL;
        mSdkHandler.sendMessage(msg);
    }

    public void makeCall(String number, String password) {
        MakeCallParam param = new MakeCallParam();
        param.callType = 1;
        param.uri = number;
        param.password = (password == null ? "" : password);
        param.signalType = 2;
        param.displayName = number;

        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_MAKE_CALL;
        message.getData().putParcelable(AppCons.BundleKeys.EXTRA_DATA, param);
        mSdkHandler.sendMessage(message);
    }

    public void reloadHardware() {
        mSdkHandler.sendEmptyMessage(SdkHandler.HANDLER_SDK_RELOAD_CAMERA);
    }

    public void answerCall(String number, String password) {
        MakeCallParam param = new MakeCallParam();
        param.callType = 1;
        param.uri = number;
        param.password = (password == null ? "" : password);
        param.signalType = 2;
        param.displayName = number;

        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_ANSWER_CALL;
        message.getData().putParcelable(AppCons.BundleKeys.EXTRA_DATA, param);
        mSdkHandler.sendMessage(message);
    }

    public void enableVideo(boolean enable) {
        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_TOGGLE_VIDEO_MUTE;
        message.arg1 = enable ? 1 : 0;
        mSdkHandler.sendMessage(message);
    }

    public void muteMic(boolean mute) {
        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_MIC_MUTE;
        message.arg1 = mute ? 1 : 0;
        mSdkHandler.sendMessage(message);
    }

    //Speaker:2 ,Gallery:1
    public void setLayoutMode(int mode) {
        AppSettings.getInstance().setSpeakerMode(mode == 2);
        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_SET_LAYOUT_MODE;
        message.arg1 = mode;
        mSdkHandler.sendMessage(message);
    }

    public void getUserInfo(){
        Message msg = Message.obtain(mSdkHandler);
        msg.what = SdkHandler.HANDLER_USER_MESSAGE;
        msg.sendToTarget();
    }

    public void endCall() {
        mSdkHandler.sendEmptyMessage(SdkHandler.HANDLER_SDK_DROP_CALL);
    }
    public void switchCamera() {
        mSdkHandler.sendEmptyMessage(SdkHandler.HANDLER_SDK_SWITCH_CAMERA);
    }

    public void logout() {
        userInLogin = false;
        loginInLoop(false);
        mSdkHandler.sendEmptyMessage(SdkHandler.HANDLER_LOGOUT);
    }
    public void requestHandUp() {
        mSdkHandler.sendEmptyMessage(SdkHandler.HANDLER_HAND_UP);
    }

    public void phoneStateChange(boolean isPhoneChang) {
        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_USER_IMAGE;
        message.arg1 = isPhoneChang ? 1 : 0;
        mSdkHandler.sendMessage(message);
    }

    public void enableSpeaker(boolean speaker) {
        Message message = Message.obtain();
        message.what = SdkHandler.HANDLER_SDK_USER_SPEAKER;
        message.arg1 = speaker ? 1 : 0;
        mSdkHandler.sendMessage(message);
    }

    private SurfaceTask contentSurfaceTask;

    public void setContentViewToSdk(SurfaceView surfaceView) {
        if(contentSurfaceTask != null) {
            mSdkHandler.removeCallbacks(contentSurfaceTask);
        }
        if(surfaceView == null) {
            sdkManager.setContentSurface(null);
        } else {
            contentSurfaceTask = new SurfaceTask(surfaceView) {
                @Override
                protected void injectSurface(SurfaceView surfaceView) {
                    sdkManager.setContentSurface(surfaceView);
                }
            };

            mSdkHandler.post(contentSurfaceTask);
        }
    }

    private SurfaceTask localSurfaceTask;

    public void setLocalViewToSdk(SurfaceView surfaceView) {
        if(localSurfaceTask != null) {
            mSdkHandler.removeCallbacks(localSurfaceTask);
        }
        if(surfaceView == null) {
            sdkManager.setLocalSurface(null);
        } else {
            localSurfaceTask = new SurfaceTask(surfaceView) {
                @Override
                protected void injectSurface(SurfaceView surfaceView) {
                    sdkManager.setLocalSurface(surfaceView);
                }
            };

            mSdkHandler.post(localSurfaceTask);
        }
    }

    private RemoteSurfaceTask remoteSurfaceTask;

    public void setRemoteViewToSdk(Object[] surfaceView) {
        if(remoteSurfaceTask != null) {
            mSdkHandler.removeCallbacks(remoteSurfaceTask);
        }
        if(surfaceView == null) {
            sdkManager.setRemoteSurface(null);
        } else {
            sdkManager.setRemoteSurface(surfaceView);
            mSdkHandler.post(remoteSurfaceTask);
        }
    }

    private SurfaceTask previewSurfaceTask;

    public void setPreviewToSdk(SurfaceView surfaceView) {
        if(previewSurfaceTask != null) {
            mSdkHandler.removeCallbacks(previewSurfaceTask);
        }
        if(surfaceView == null) {
            sdkManager.setPreSurface(null);
        } else {
            previewSurfaceTask = new SurfaceTask(surfaceView) {
                @Override
                protected void injectSurface(SurfaceView surfaceView) {
                    sdkManager.setPreSurface(surfaceView);
                }
            };

            mSdkHandler.post(previewSurfaceTask);
        }
    }

    public void startMediaStaticsLoop() {
        stopMediaStaticsLoop();
        mSdkHandler.sendEmptyMessage(SdkHandler.HANDLER_SDK_GET_STATISTICS);
    }

    public void stopMediaStaticsLoop() {
        mSdkHandler.removeMessages(SdkHandler.HANDLER_SDK_GET_STATISTICS);
    }

    public void loginInLoop(boolean start) {

        mSdkHandler.removeMessages(SdkHandler.HANDLER_AUTO_LOGIN);
        if(start) {
            mSdkHandler.sendEmptyMessageDelayed(SdkHandler.HANDLER_AUTO_LOGIN, 1000);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRegisterEvent(RegisterState state) {
        loginInLoop(false);
        if (state == RegisterState.FAILED && userInLogin) {
            loginInLoop(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onLoginRetryEvent(LoginRetryEvent event) {
        if (userInLogin) {
            loginInLoop(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onLoginResultEvent(LoginResultEvent event) {
        if (userInLogin && event.isAnonymous() && event.getCode() == LoginResultEvent.LOGIN_ANONYMOUS_FAILED) {
            LOG.error("Reconnect anonymous login failed : "+ event.getMessage());
            loginInLoop(true);
        }
    }

    public void setUserInLogin(boolean userInLogin) {
        this.userInLogin = userInLogin;
    }

    public void uploadAvatar(String filePath) {
        Message msg = Message.obtain(mSdkHandler);
        msg.what = SdkHandler.HANDLER_UPLOAD_AVATAR;
        msg.obj = filePath;
        msg.sendToTarget();
    }

    public void updateUserImage(String imagePath) {
        Message msg = Message.obtain(mSdkHandler);
        msg.what = SdkHandler.HANDLER_SDK_SAVE_USER_IMAGE;
        msg.obj = imagePath;
        msg.sendToTarget();
    }

    public void userRename(String newName) {
        Message msg = Message.obtain(mSdkHandler);
        msg.what = SdkHandler.HANDLER_USER_RENAME;
        msg.obj = newName;
        msg.sendToTarget();
    }

    public void updatePassword(String oldPassword ,String newPassword) {
        Message msg = Message.obtain();
        msg.what = SdkHandler.HANDLER_USER_PASSWORD;
        Bundle bundle = new Bundle();
        bundle.putString("oldPwd",oldPassword);
        bundle.putString("newPwd",newPassword);
        msg.setData(bundle);
        mSdkHandler.sendMessage(msg);
    }

    public void obtainLogPath(){
        Message msg = Message.obtain(mSdkHandler);
        msg.what = SdkHandler.HANDLER_USER_LOGPATH;
        msg.sendToTarget();
    }

    public void NetworkQuality(){
        Message msg = Message.obtain(mSdkHandler);
        msg.what = SdkHandler.HANDLER_NETWORK_STATUS;
        msg.sendToTarget();
    }

    public boolean isCalling(){
      return   sdkManager.isCalling();
    }
    public void zoomVideoByStreamType(EVEngine.StreamType type,float factor,float cx,float cy){
        sdkManager.zoomVideoByStreamType(type,factor,cx,cy);
    }
    public void showFloatIndicator() {
        ((HjtApp)getApplication()).startFloatService();
    }

    public void cancelFloatIndicator() {
        ((HjtApp)getApplication()).stopFloatService();
    }

    public void cancelNotification() {
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(AppCons.APP_ID);
    }

    private class HeadsetPlugReceiver extends BroadcastReceiver {
        private int value = CopyAssets.EVENT_STOP;
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = null;
            LOG.info("HeadsetPlugReceiver "+intent.getAction().toString());
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                event = CopyAssets.HEADSET_PLUG_EVENT;
                if (intent.getIntExtra("state", 0) != 1) { // plug out
                    LOG.info("HeadsetPlugReceiver onReceive, wiredHeadset plug out");
                    value = CopyAssets.EVENT_STOP;
                } else { // plug in
                    LOG.info("HeadsetPlugReceiver onReceive, wiredHeadset plug in");
                    value = CopyAssets.EVENT_START;
                }
                if (isInitialStickyBroadcast()) {
                    LOG.info("HeadsetPlugReceiver onReceive, isInitialStickyBroadcast wiredHeadset plug, value="
                            + value + ", ignore.");
                    return;
                }
            } else if (intent.getAction().equals(AppCons.BLUETOOTH_CONNECT_ACTION)) {
                event = CopyAssets.BLUETOOTH_CONNECTION_EVENT;
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    LOG.info("HeadsetPlugReceiver onReceive, bluetooth connected");
                    value = CopyAssets.EVENT_START;
                } else {
                    LOG.info("HeadsetPlugReceiver onReceive, bluetooth disconnected");
                    value = CopyAssets.EVENT_STOP;
                }
            }else if (intent.getAction().equals(AppCons.BLUETOOTH_STATE_CHNGED)) {
                event = CopyAssets.BLUETOOTH_CONNECTION_EVENT;
                LOG.info("HeadsetPlugReceiver onReceive, bluetooth disconnected state changed");
                value = CopyAssets.EVENT_STOP;
            }
            if (event != null) {
                LOG.info("appservice : "+event+"==="+value);
                String audioRoute = CopyAssets.getInstance().processAudioRouteEvent(event, value);
                updateSpeakerStatus(audioRoute);
            }
        }
    }

    private void updateSpeakerStatus(String audioRoute) {
      //  LOG.info("appservice updateSpeakerStatus : "+audioRoute);
        boolean isSpeakerOn = true;
        if (audioRoute.equals(CopyAssets.ROUTE_TO_SPEAKER)) {
            isSpeakerOn = true;
        } else if (audioRoute.equals(CopyAssets.ROUTE_TO_RECEIVER)) {
            isSpeakerOn = false;
        } else { // bluetooth or wiredHeadset
            isSpeakerOn = false;
        }
        HjtApp.setSpeakerOn(isSpeakerOn);
    }

    boolean isHeadsetBroadCastRegistered = false;
    public void initAudioMode(boolean isVideoCall) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AppCons.BLUETOOTH_CONNECT_ACTION);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(headsetPlugReceiver, intentFilter);
        CopyAssets.getInstance().processAudioRouteEvent(
                isVideoCall ? CopyAssets.CONVERSATION_EVENT : CopyAssets.CONVERSATION_AUDIOONLY_EVENT,
                CopyAssets.EVENT_START);
        isHeadsetBroadCastRegistered = true;
    }

    public void uninitAudioMode(boolean isVideoCall) {
        if(isHeadsetBroadCastRegistered) {
            unregisterReceiver(headsetPlugReceiver);
            CopyAssets.getInstance().processAudioRouteEvent(
                    isVideoCall ? CopyAssets.CONVERSATION_EVENT : CopyAssets.CONVERSATION_AUDIOONLY_EVENT,
                    CopyAssets.EVENT_STOP);
            isHeadsetBroadCastRegistered = false;
        }
    }
    public void startAudioMode(boolean isVideoCall){
        CopyAssets.getInstance().processAudioRouteEvent(
                isVideoCall ? CopyAssets.CONVERSATION_EVENT : CopyAssets.CONVERSATION_AUDIOONLY_EVENT,
                CopyAssets.EVENT_START);
    }

}
