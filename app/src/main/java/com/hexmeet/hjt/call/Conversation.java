package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.FullscreenActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.EmMessageCache;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.conf.MeetingForWechat;
import com.hexmeet.hjt.conf.WeChat;
import com.hexmeet.hjt.event.AudioMode;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.ContentEvent;
import com.hexmeet.hjt.event.EmLoginSuccessEvent;
import com.hexmeet.hjt.event.LiveEvent;
import com.hexmeet.hjt.event.MicMuteChangeEvent;
import com.hexmeet.hjt.event.MicMuteUpdateEvent;
import com.hexmeet.hjt.event.MuteSpeaking;
import com.hexmeet.hjt.event.NetworkEvent;
import com.hexmeet.hjt.event.NetworkStatusEvent;
import com.hexmeet.hjt.event.PeopleNumberEvent;
import com.hexmeet.hjt.event.RecordingEvent;
import com.hexmeet.hjt.event.RefreshLayoutModeEvent;
import com.hexmeet.hjt.event.RemoteMuteEvent;
import com.hexmeet.hjt.event.RemoteNameEvent;
import com.hexmeet.hjt.event.SvcSpeakerEvent;
import com.hexmeet.hjt.event.UserInfoEvent;
import com.hexmeet.hjt.groupchat.GroupChatActivity;
import com.hexmeet.hjt.sdk.ChannelStatList;
import com.hexmeet.hjt.sdk.CopyAssets;
import com.hexmeet.hjt.sdk.MessageOverlayInfo;
import com.hexmeet.hjt.sdk.SvcLayoutInfo;
import com.hexmeet.hjt.service.MeetingWindowService;
import com.hexmeet.hjt.service.ScreenCaptureService;
import com.hexmeet.hjt.service.SharedState;
import com.hexmeet.hjt.utils.JsonUtil;
import com.hexmeet.hjt.utils.PermissionUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.RequiresApi;
import ev.common.EVFactory;

public class Conversation extends FullscreenActivity {
    private Logger LOG = Logger.getLogger(this.getClass());
    private final static int ON_SVC_CONTENT_CHANGED = 10;
    private final static int ON_SVC_LAYOUT_CHANGED = 11;
    private final static int ON_SVC_SPEAKER_CHANGED = 12;
    private final static int ON_SVC_MIC_MUTE_CHANGED = 13;
    private final static int ON_SVC_MESSAGE_OVERLAY = 14;
    private final static int ON_SVC_REFRESH_LAYOUT_MODE = 15;
    private final static int ON_SVC_NETWORK_TOAST = 16;
    private final static int ON_SVC_PEOPLE_NUMBER = 17;
    private final static int ON_SVC_MICROPHONEMUTED = 18;
    private final static int ON_SVC_REMOTE_UPDATE_NAME = 19;
    public final static int ON_SVC_FLOAT_WINDOW = 20;
    private final static int REQUEST_CODE = 100;
    private SurfaceView mDummyPreviewView; // this surface is used to capture image from camera

    private long startTime;
    private boolean isVideoCall = true;
    private VideoBoxGroup videoBoxGroup;

    private SignalIntensityScanner signalIntensityScanner;
    private CallStaticsWindow callStaticsWindow;
    private ConfManageWindow confManageWindow;

    private OrientationEventListener orientationListener;

    private HandlerThread handlerThread = new HandlerThread("conversation_worker");

    private ConversationController controller;
    private boolean isDestroying = false;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private TextView recordView, networkConditionToast;
    private AudioManager audio;
    private TextView audioSpeakName;
    private TextView microphoneMuted;
    private MeetingWindowService floatService;
    private MediaProjectionManager mProjectionManager;
    private boolean isCheckResumeEvent = false;//检测是否有双流
    private boolean isBind = false; //是否绑定服务
    private boolean showFloatWindow = false;

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.info("Call Screen onCreate");
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        hideNavigationBar(new Handler());
        HjtApp.getInstance().setConversation(this);
        this.setContentView(R.layout.converstation);

        audioSpeakName =(TextView) findViewById(R.id.audio_name);
        HjtApp.getInstance().startFloatService();
        floatWindowService();

        if(!isCheckResumeEvent){
            LOG.info("onCreate isCheckResumeEvent()");
            isCheckResumeEvent = true;
            resumeEvent();
        }

        startTime = SystemCache.getInstance().getStartTime();
        recordView = (TextView)findViewById(R.id.record_view);
        networkConditionToast = (TextView)findViewById(R.id.network_condition_toast);
        microphoneMuted = (TextView) findViewById(R.id.mute_speaking);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audio.setMicrophoneMute(false);

        recordView.setVisibility(SystemCache.getInstance().isRecordingOn() ? View.VISIBLE : View.GONE);
        recordView.setText(SystemCache.getInstance().isRecording() ? getText(R.string.live) :getText(R.string.record));
        controller = new ConversationController(findViewById(R.id.control_layout), iController, getScreenWidth());

        initGesture();
        controller.startTime(startTime);
        if(SystemCache.getInstance().getPeer()!=null){
            isVideoCall = SystemCache.getInstance().getPeer().isVideoCall();
            if(SystemCache.getInstance().getPeer().getName()!=null){
                controller.setRoomNum(SystemCache.getInstance().getPeer().getName());
            }
        }


        controller.setNumber(SystemCache.getInstance().getParticipant());
        signalIntensityScanner = new SignalIntensityScanner();
        if(SystemCache.getInstance().isRemoteMuted()){
            updateMenu(true);
        }

        mProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);


        if (!isVideoCall) {
            controller.updateAsAudioCall();
        }

        videoBoxGroup = new VideoBoxGroup((RelativeLayout) findViewById(R.id.root));
        svcHandler.postDelayed(buildTask, 500);

        //判断是否是语音模式
        LOG.info("isUserVideoMode : "+SystemCache.getInstance().isUserVideoMode());
        //前提必须是否支持语音模式在进行判断
        if(SystemCache.getInstance().getFeatureSupport()!=null && SystemCache.getInstance().getFeatureSupport().isSwitchingToAudioConference()){
            controller.showVideoMode(SystemCache.getInstance().isUserVideoMode());
            if(SystemCache.getInstance().getSpeakName()!=null && !SystemCache.getInstance().getSpeakName().equals("")){
                audioSpeakName.setText(SystemCache.getInstance().getSpeakName()+"  "+getString(R.string.speaking));
                audioSpeakName.setVisibility(SystemCache.getInstance().isUserVideoMode() ? View.GONE : View.VISIBLE );
            }
        }

        mDummyPreviewView = (SurfaceView) findViewById(R.id.dummyPreviewView);
        if (isVideoCall) {
            signalIntensityScanner.sendEmptyMessageDelayed(1, 5000);

            mDummyPreviewView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    LOG.info("mDummyPreviewView display surface is being changed. format: " + format
                            + ", width: " + width + ", height: " + height + ", surface: " + holder.getSurface());
                    HjtApp.getInstance().getAppService().setPreviewToSdk(mDummyPreviewView);
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    LOG.info("mDummyPreviewView display surface created");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    LOG.info("mDummyPreviewView display surface destroyed");
                    HjtApp.getInstance().getAppService().setPreviewToSdk(null);
                    if (isVideoCall && HjtApp.getInstance().getAppService().isCalling() && !SystemCache.getInstance().isSharedScreen()) {
                        startFloat();
                    }
                }
            });
        } else {
            signalIntensityScanner.sendEmptyMessageDelayed(2, 5000);
        }

        HjtApp.setSpeakerOn(!isEarphoneOn());

        if (isVideoCall && orientationListener == null) {
            orientationListener = new OrientationEventListener(Conversation.this) {
                private int oldDirection = 0;
                private int oldCameraDirection = -1;

                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                        return;
                    }

                    int direction = 0;
                    if ((orientation >= 0 && orientation <= 40)
                            || (orientation >= 320 && orientation <= 360)) {
                        direction = 0;
                    } else if (orientation >= 50 && orientation <= 130) {
                        direction = 90;
                    } else if (orientation >= 140 && orientation <= 220) {
                        direction = 180;
                    } else if (orientation >= 230 && orientation <= 310) {
                        direction = 270;
                    } else {
                        return;
                    }

                    if (Math.abs(oldDirection - orientation) >= 50) {
                        if (direction != oldDirection) {
                            onNewDirection(direction);
                            oldDirection = direction;
                        }
                    }
                }

                private void onNewDirection(final int direction) {
                    if (direction != oldCameraDirection) {
                        final int _cameraDirection = (360 - direction) % 360;
                        if(HjtApp.getInstance().getAppService()!=null){
                            Handler handler = new Handler(handlerThread.getLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    HjtApp.getInstance().getAppService().cameraDirection(_cameraDirection);
                                }
                            });
                            oldCameraDirection = direction;
                        }

                    }
                }
            };

            orientationListener.enable();
        }

        handlerThread.start();
        callStaticsWindow = new CallStaticsWindow(this);
    }

    private Runnable buildTask = new Runnable() {
        @Override
        public void run() {
            videoBoxGroup.buildDefaultCells(!EVFactory.createEngine().micEnabled());
        }
    };

    private void resumeEvent() {
        if(SystemCache.getInstance().getSvcLayoutInfo() != null) {
            LOG.info("resumeEvent SvcLayoutInfo");
            onSvcLayoutChangedEvent(SystemCache.getInstance().getSvcLayoutInfo());
        }
        onContentEvent(new ContentEvent(SystemCache.getInstance().withContent()));
        if(SystemCache.getInstance().getOverlayMessage() != null) {
            LOG.info("resumeEvent OverlayMessage");
            onMessageOverlayEvent(SystemCache.getInstance().getOverlayMessage());
        }

        if(SystemCache.getInstance().getRemoteNameEvent()!=null){
            LOG.info("resumeEvent RemoteName");
            onRemoteNameEvent(SystemCache.getInstance().getRemoteNameEvent());
        }
    }


    private ConversationController.IController iController = new ConversationController.IController() {
        @Override
        public void updateCellsAsLayoutModeChanged() {
            if(videoBoxGroup != null) {
                videoBoxGroup.onLayoutModeChanged();
            }
        }

        @Override
        public void onVideoSwitchClick(boolean isVideo) {
           LOG.info("ISVIDEO : "+isVideo);
            videoBoxGroup.updateContent(!isVideo);
            //isRecordVisible(!isVideo);
        }

        @Override
        public void showMediaStatistics() {
            if(callStaticsWindow != null) {
                callStaticsWindow.show();
            }
        }

        @Override
        public void showLocalCamera(boolean show) {
            if(videoBoxGroup != null) {
                LOG.info("showLocalCamera : "+show);
                videoBoxGroup.showLocalCamera(show);
            }
        }

        @Override
        public void showConferenceManager() {
            confManageWindow = new ConfManageWindow(Conversation.this);
            confManageWindow.show();
        }

        @Override
        public void updateMarginTopForMessageOverlay(int margin) {
            if(videoBoxGroup != null) {
                videoBoxGroup.updateMessageView(margin);
            }
        }

        @Override
        public void updateCellLocalMuteState(boolean isMute) {
            LOG.info("updateCellLocalMuteState : "+isMute);
            videoBoxGroup.updateLocalMute(isMute);
        }

        @Override
        public void switchVoiceMode(boolean isVoiceMode) {
            LOG.info("switchVoiceMode : "+isVoiceMode);
            SystemCache.getInstance().setUserVideoMode(isVoiceMode);
            HjtApp.getInstance().getAppService().setVideoMode(isVoiceMode);
            //开启或隐藏audio按钮
          //  audioMode.setVisibility(isVoiceMode ? View.GONE :  View.VISIBLE );
            if(videoBoxGroup != null) {
                videoBoxGroup.updateAudioView(isVoiceMode);
            }
            //切换为语音状态下 显示SpeakName
            if(SystemCache.getInstance().getSpeakName()!=null && !SystemCache.getInstance().getSpeakName().equals("")){
                audioSpeakName.setText(SystemCache.getInstance().getSpeakName()+"  "+getString(R.string.speaking));
                audioSpeakName.setVisibility(isVoiceMode ? View.GONE : View.VISIBLE );
            }


        }

        @Override
        public void showChat() {
            LOG.info("showChat()");
            boolean ok = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(HjtApp.getInstance().getContext());
            if(!ok){
                if(!isDestroyed()){
                    meetingDialog(MeetingDialog.MEETING_PERMISSION);
                }
            }else {
                GroupChatActivity.actionStart(Conversation.this);
            }

        }

        @Override
        public void changeUserName() {
            LOG.info("changeUserName()");
            meetingDialog(MeetingDialog.MEETING_UPDATE_NAME);
        }

        @Override
        public void onClickShareScreen() {//共享屏幕
          if(!SystemCache.getInstance().isSharedScreen()){
              boolean ok = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(HjtApp.getInstance().getContext());
              if(!ok){
                  if(!isDestroyed()){
                      meetingDialog(MeetingDialog.MEETING_PERMISSION);
                  }
              }else {
                  startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                          REQUEST_CODE);
              }
            }else {
              stopScreenCapture();
            }
        }

        @Override
        public void onNoChangeLayout() {
            svcHandler.removeMessages(ON_SVC_MICROPHONEMUTED);
            microphoneMuted.setText(R.string.layout_mode_disable);
            microphoneMuted.setVisibility(View.VISIBLE);
            Message msg = Message.obtain();
            msg.what = ON_SVC_MICROPHONEMUTED;
            svcHandler.sendMessageDelayed(msg, 2000);
        }

        @Override
        public void onHangUp() {
            LOG.info("meeting host : "+HjtApp.getInstance().getAppService().isMeetingHost());
            if(HjtApp.getInstance().getAppService().isMeetingHost()){
                meetingDialog(MeetingDialog.MEETING_END);
            }else {
                meetingDialog(MeetingDialog.MEETING_LEAVE);
            }
        }
    };

    public void  stopScreenCapture(){
        stopScreenCaptureService();
        controller.shareScreen(false);
        SystemCache.getInstance().setSharedScreen(false);
    }


    private boolean isEarphoneOn() {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        return am.isWiredHeadsetOn();
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return Math.max(displayMetrics.heightPixels, displayMetrics.widthPixels);
    }

    @Override
    protected void onStart() {
        LOG.info("onStart");
        super.onStart();
        if(floatService!=null){
            showFloatWindow = false;
            floatService.stopFloatWindow();
        }

        if(!isCheckResumeEvent){
            LOG.info("onStart isCheckResumeEvent()");
            isCheckResumeEvent = true;
            resumeEvent();
        }

        if(SystemCache.getInstance().isSharedScreen()){
            controller.shareScreen(true);
         }else {
            controller.shareScreen(false);
            stopScreenCaptureService();
            if(SystemCache.getInstance().isSharedPermission()){//是否有权限共享
                EventBus.getDefault().post(SharedState.NOPERMISSION);
                SystemCache.getInstance().setSharedPermission(false);
            }
        }


        if (isVideoCall) {
            LOG.info("isLocalCamera : "+SystemCache.getInstance().isCamera());
            if(SystemCache.getInstance().isCamera() && HjtApp.getInstance().getAppService()!=null){
                HjtApp.getInstance().getAppService().muteVideo(true);
            }
            boolean isLocalVideoMuted = EVFactory.createEngine().cameraEnabled();
            controller.muteVideo(!isLocalVideoMuted);
        }
        if(HjtApp.getInstance().getAppService()!=null){ 
           // HjtApp.getInstance().getAppService().startAudioMode(true);

            if(SystemCache.getInstance().isMuteMic()) {//判断来电前是否是非静音状态
                HjtApp.getInstance().getAppService().muteMic(false);
            }
            boolean isLocalMicMuted = HjtApp.getInstance().getAppService().micEnabled();
            LOG.info("isLocalMicMuted :" + isLocalMicMuted);//true 静音 ，false  非静音
            controller.muteMic(!isLocalMicMuted);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LOG.info("dispath : "+event);
        return super.dispatchKeyEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStop() {
       PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();//是否是亮屏状态
        LOG.info("isScreenOn : "+isScreenOn);
        if(isScreenOn){
            LOG.info("onStop isCalling ? "+HjtApp.getInstance().getAppService().isCalling());//false 手动挂断
            if (isVideoCall && HjtApp.getInstance().getAppService().isCalling() && !SystemCache.getInstance().isSharedScreen()) {
                startFloat();
            }
            if(SystemCache.getInstance().isSharedScreen()){
                closeLocalVideo();
                HjtApp.getInstance().getAppService().enableVideo(false);
            }
        }
        isCheckResumeEvent = false;
        super.onStop();
    }

    private void startFloat(){
        if(!showFloatWindow){
            closeLocalVideo();
            if(floatService!=null){
                LOG.info("show float windows");
                showFloatWindow = true;
                floatService.svcHandler.sendEmptyMessage(ON_SVC_FLOAT_WINDOW); //floatService.createFloatView();
                HjtApp.getInstance().getAppService().enableVideo(false);
            }
        }
    }

    public void closeLocalVideo(){
        SystemCache.getInstance().setCamera(EVFactory.createEngine().cameraEnabled());
        SystemCache.getInstance().setUserMuteVideo(false);
    }

    @Override
    protected void onPause() {
        LOG.info("onPause "+isFinishing());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOG.info("onResume");
        svcHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                controller.hideBar();
            }
        }, 15000);
    }

    @Override
    protected void onDestroy() {
        LOG.info("onDestroy");
        if(isBind && connections!=null){
            LOG.info("unbindService()");
            unbindService(connections);
            isBind = false;
            showFloatWindow = false;
        }
        if(EVFactory.createEngine().getCallInfo()!=null) {
            clearResource();
        }

        svcHandler.removeCallbacksAndMessages(null);
        signalIntensityScanner.removeCallbacksAndMessages(null);

        if (orientationListener != null) {
            orientationListener.disable();
            orientationListener = null;
        }
        if(dialog != null) {
            dialog.dismiss();
        }

        handlerThread.quit();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        HjtApp.getInstance().setConversation(null);
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class SignalIntensityScanner extends Handler {

        @Override
        public void handleMessage(Message msg) {

            if (!HjtApp.isScreenLocked() && HjtApp.isForeground()) {
                    if (msg.what == 2) {
                        // scan signal intensity only when title bar is visible
                        signalIntensityScanner.sendEmptyMessageDelayed(2, 2000);
                        return;
                    }
                }

                if (msg.what == 1) {
                    // scan signal intensity in background all along with Conversation
                    signalIntensityScanner.sendEmptyMessageDelayed(1, 5000);
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {

            Intent intents = new Intent(this, ScreenCaptureService.class);
            intents.putExtra("code", resultCode+"");
            intents.putExtra("data", data);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intents);
            } else {
                startService(intents);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioEvent(AudioMode event) {
        LOG.info("onAudioEvent ");
        if(event != null) {
            controller.showVideoMode(!SystemCache.getInstance().isUserVideoMode());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent event) {
        svcHandler.removeMessages(ON_SVC_NETWORK_TOAST);
        LOG.info("warning weak signal intensity");
        if(event.getCode()==NetworkEvent.EV_WARN_NETWORK_POOR){
            networkConditionToast.setText(R.string.network_instability);
        }else if(event.getCode()==NetworkEvent.EV_WARN_NETWORK_VERY_POOR) {
            networkConditionToast.setText(R.string.network_very_poor);
        }else if(event.getCode()==NetworkEvent.EV_WARN_BANDWIDTH_INSUFFICIENT) {
            networkConditionToast.setText(R.string.bandwidth_insufficient);
        }else if(event.getCode()==NetworkEvent.EV_WARN_BANDWIDTH_VERY_INSUFFICIENT) {
            networkConditionToast.setText(R.string.bandwidth_very_insufficient);
        }else if(event.getCode()==NetworkEvent.EV_WARN_UNMUTE_AUDIO_NOT_ALLOWED) {
            svcHandler.removeMessages(ON_SVC_MICROPHONEMUTED);
            microphoneMuted.setText(R.string.audio_not_allowed);
            microphoneMuted.setVisibility(View.VISIBLE);
            Message msg = Message.obtain();
            msg.what = ON_SVC_MICROPHONEMUTED;
            svcHandler.sendMessageDelayed(msg, 2000);
            return;
        }else if(event.getCode()==NetworkEvent.EV_WARN_UNMUTE_AUDIO_INDICATION) {
            //unmuteAudioDialog(true);
            if(dialog!=null){
                dialog.dismiss();
            }
            meetingDialog(MeetingDialog.MEETING_UNMUTE);
            return;
        }
        networkConditionToast.setVisibility(View.VISIBLE);
        Message msg = Message.obtain();
        msg.what = ON_SVC_NETWORK_TOAST;
        svcHandler.sendMessageDelayed(msg, 10000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkStatusEvent(NetworkStatusEvent event) {
        LOG.info("scan signal intensity, level=" + event.getNetwork());
        controller.updateSignalLevel(event.getNetwork());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSvcLayoutChangedEvent(SvcLayoutInfo info) {
        LOG.info("onSvcLayoutChangedEvent showFloatWindow :" + showFloatWindow);
        if(!showFloatWindow){
            LOG.info("SvcLayoutInfo : " + info.toString());
            svcHandler.removeMessages(ON_SVC_LAYOUT_CHANGED);
            showSpeakName(info.getSpeakerIndex(),info.getSpeakerName());
            Message msg = Message.obtain();
            msg.what = ON_SVC_LAYOUT_CHANGED;
            msg.getData().putParcelable(AppCons.BundleKeys.EXTRA_DATA, info);
            svcHandler.sendMessage(msg);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSvcSpeakerChangedEvent(SvcSpeakerEvent event) {
        LOG.info("onSvcSpeakerChangedEvent showFloatWindow :" + showFloatWindow);
        if(!showFloatWindow) {
            svcHandler.removeMessages(ON_SVC_SPEAKER_CHANGED);
            LOG.info("onSvcSpeakerChangedEvent : " + event.toString());
            showSpeakName(event.getIndex(), event.getSiteName());
            Message msg = Message.obtain();
            msg.what = ON_SVC_SPEAKER_CHANGED;
            msg.arg1 = event.getIndex();
            msg.obj = event.getSiteName();
            svcHandler.sendMessage(msg);
        }
    }

    private void showSpeakName(int index,String displayName) {
        LOG.info("showSpeakName : "+displayName+",index :"+index+",isUserVideo : "+SystemCache.getInstance().isUserVideoMode());
        SystemCache.getInstance().setSpeakName(displayName);
        if(!SystemCache.getInstance().isUserVideoMode()&& displayName!=null && !displayName.equals("")){
            setSpeakName(displayName);
        }else if (SystemCache.getInstance().isUserVideoMode()&& index == -1 && (displayName!=null && !displayName.equals(""))){
            setSpeakName(displayName);
        } else {
            audioSpeakName.setVisibility(View.GONE);
        }
    }

    private void setSpeakName(String name) {
        LOG.info("setSpeakName : "+name);
        audioSpeakName.setVisibility(View.VISIBLE);
        audioSpeakName.setText(name+"  "+getString(R.string.speaking));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordingEvent(RecordingEvent event) {
        LOG.info("onRecording = "+ event);
        if(event != null) {
            recordView.setVisibility(event == RecordingEvent.ON ? View.VISIBLE : View.GONE);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveEvent(LiveEvent event) {
        LOG.info("onLiveEvent = "+ event.toString());
        if(event != null) {
            recordView.setText(event.isRecording() ? getText(R.string.live) : getText(R.string.record));
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRefreshLayoutModeEvent(RefreshLayoutModeEvent event) {
        if(event != null && event.isSpeakerMode()) {
            svcHandler.sendEmptyMessage(ON_SVC_REFRESH_LAYOUT_MODE);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRemoteMicMuteEvent(MicMuteUpdateEvent event) {
        LOG.info("onRemoteMicMuteEvent showFloatWindow :" + showFloatWindow);
        if(!showFloatWindow) {
            Message msg = Message.obtain();
            msg.what = ON_SVC_MIC_MUTE_CHANGED;
            msg.getData().putString("participants", event.getParticipants());
            svcHandler.sendMessage(msg);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageOverlayEvent(MessageOverlayInfo event) {
        svcHandler.removeMessages(ON_SVC_MESSAGE_OVERLAY);
        Message msg = Message.obtain();
        msg.what = ON_SVC_MESSAGE_OVERLAY;
        msg.getData().putParcelable(AppCons.BundleKeys.EXTRA_DATA, event);
        svcHandler.sendMessageDelayed(msg, 500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaStatisticsEvent(ChannelStatList channelStatList) {
        LOG.info("onMediaStatisticsEvent");
        if(callStaticsWindow != null) {
            callStaticsWindow.updateMediaStatistics(channelStatList);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("event : "+event.getCallState());
        if (event.getCallState() == CallState.IDLE) {
            AppSettings.getInstance().setSpeakerMode(false);
            CopyAssets.getInstance().unRegisterAudio();
            svcHandler.removeCallbacks(buildTask);
            clearResource();
            isDestroying = true;
            resetCache();
            LOG.info("isAnonymousMakeCall : "+SystemCache.getInstance().isAnonymousMakeCall());
            if(SystemCache.getInstance().isAnonymousMakeCall()) {
                clearAnonymousData();
            }
            EmMessageCache.getInstance().resetIMCache();
            HjtApp.getInstance().stopFloatService();
            // after a call we try to login back to original login-ed server
            //LoginService.getInstance().autoLogin();

            finish();
        }
    }
    private void resetCache(){
        SystemCache.getInstance().setParticipant(null);
        SystemCache.getInstance().setUserVideoMode(true);
        SystemCache.getInstance().setSpeakName(null);
        SystemCache.getInstance().setRemoteMuted(false);
    }

    private void clearResource() {
        if(callStaticsWindow != null) {
            callStaticsWindow.dismiss();
            callStaticsWindow.clean();
            callStaticsWindow = null;
        }

        controller.clean();
        videoBoxGroup.showMessage(false);
        videoBoxGroup.release();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginRespEvent(UserInfoEvent event) {
        LOG.info("confManageWindow  updateTokenForWeb()");
        if(confManageWindow != null){
            confManageWindow.updateTokenForWeb();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(ContentEvent event) {
       // ture 开启  false 关闭
        svcHandler.removeMessages(ON_SVC_CONTENT_CHANGED);
        Message msg = Message.obtain();
        msg.what = ON_SVC_CONTENT_CHANGED;
        msg.arg1 = event.withContent() ? 1 :0;
        svcHandler.sendMessageDelayed(msg, 500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMuteSpeakingDetected(MuteSpeaking userMute) {
        svcHandler.removeMessages(ON_SVC_MICROPHONEMUTED);
        if(userMute.isMuteSpeaking()){
            microphoneMuted.setText(R.string.mute_speaking);
            microphoneMuted.setVisibility(View.VISIBLE);
            Message msg = Message.obtain();
            msg.what = ON_SVC_MICROPHONEMUTED;
            svcHandler.sendMessageDelayed(msg, 2000);
        }
    }
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPeopleNumber(PeopleNumberEvent numberEvent) {
        LOG.info("peopleNumber : "+numberEvent.getNumber());
        svcHandler.removeMessages(ON_SVC_PEOPLE_NUMBER);
        Message msg = Message.obtain();
        msg.what = ON_SVC_PEOPLE_NUMBER;
        msg.obj = numberEvent.getNumber();
        svcHandler.sendMessageDelayed(msg, 500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMicMuteChangeEvent(MicMuteChangeEvent event) {
        if(controller != null) {
            controller.muteMic(event.isMute());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRemoteNameEvent(RemoteNameEvent event) {
        LOG.info("onRemoteNameEvent showFloatWindow :" + showFloatWindow);
        if(!showFloatWindow) {
            svcHandler.removeMessages(ON_SVC_REMOTE_UPDATE_NAME);
            LOG.info("RemoteNameEvent : " + event.isLocal());
            if (event != null) {
                Message msg = Message.obtain();
                msg.what = ON_SVC_REMOTE_UPDATE_NAME;
                Bundle bundle = new Bundle();
                bundle.putString("deviceId", event.getDeviceId());
                bundle.putString("displayName", event.getName());
                bundle.putBoolean("islocal", event.isLocal());
                msg.setData(bundle);
                svcHandler.sendMessageDelayed(msg, 500);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMruMuteEvent(RemoteMuteEvent event) {
        LOG.info("isMuteFromMru :  "+event.isMuteFromMru()+"");
        updateMenu(event.isMuteFromMru());
    }

    private void updateMenu(boolean remoteMuted) {
        LOG.info("microphoneMuted toast:  "+remoteMuted);
        svcHandler.removeMessages(ON_SVC_MICROPHONEMUTED);
        if(remoteMuted) {
            microphoneMuted.setText(R.string.speaking_forbidden);
        } else {
            microphoneMuted.setText(R.string.allowed_speak);
        }

        microphoneMuted.setVisibility(View.VISIBLE);
        Message msg = Message.obtain();
        msg.what = ON_SVC_MICROPHONEMUTED;
        svcHandler.sendMessageDelayed(msg, 2000);


        if(controller != null) {
            controller.updateHandUpMenu(remoteMuted);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void SharedState(SharedState state) {
        LOG.info("SharedState : "+state);
        if(state==SharedState.STOPSCREENSHARE){
            controller.shareScreen(false);
        }else if(state==SharedState.NOPERMISSION){
            SystemCache.getInstance().setSharedScreen(true);
            svcHandler.removeMessages(ON_SVC_MICROPHONEMUTED);
            microphoneMuted.setText(R.string.content_permission);
            microphoneMuted.setVisibility(View.VISIBLE);
            Message msg = Message.obtain();
            msg.what = ON_SVC_MICROPHONEMUTED;
            svcHandler.sendMessageDelayed(msg, 3000);
            stopScreenCaptureService();
        }else {
            //改变UI状态
            controller.shareScreen(true);
            SystemCache.getInstance().setSharedScreen(true);
            moveTaskToBack(true);//退到home页面
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEmLoginSuccessEvent(EmLoginSuccessEvent event) {//im登录成功
        LOG.info("onEmLoginSuccessEvent : "+event.isLoginSucceed());
        if(controller!=null){
            controller.setChatShow();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler svcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isDestroying) {
                switch (msg.what) {
                    case ON_SVC_LAYOUT_CHANGED:
                        SvcLayoutInfo svcLayoutInfo = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                        LOG.info("SvcLayoutInfo: "+svcLayoutInfo.toString());
                        if(videoBoxGroup == null || !videoBoxGroup.updateSvcLayout(svcLayoutInfo)) {
                            sendMessageDelayed(Message.obtain(msg), 1000);
                        }
                        break;
                    case ON_SVC_SPEAKER_CHANGED:
                        if(videoBoxGroup == null || !videoBoxGroup.updateSvcSpeaker(msg.arg1, (String) msg.obj)) {
                            sendMessageDelayed(Message.obtain(msg), 1000);
                        }
                        break;
                    case ON_SVC_MIC_MUTE_CHANGED:
                        if(videoBoxGroup == null || !videoBoxGroup.updateMicMute(msg.getData().getString("participants"))) {
                            sendMessageDelayed(Message.obtain(msg), 1000);
                        }
                        break;
                    case ON_SVC_CONTENT_CHANGED:
                        if(videoBoxGroup == null || !videoBoxGroup.isContentReady() || !videoBoxGroup.isRemoteCellReady()) {
                            sendMessageDelayed(Message.obtain(msg), 1000);
                        } else {
                            controller.showSwitchAsContent(msg.arg1 == 1);
                            HjtApp.getInstance().getAppService().setContentViewToSdk(msg.arg1 == 1 ? videoBoxGroup.getContentSurface() : null);
                            videoBoxGroup.updateContent(msg.arg1 == 1);
                            //isRecordVisible(msg.arg1 == 1);
                        }
                        break;

                    case ON_SVC_MESSAGE_OVERLAY:
                        MessageOverlayInfo overlayMessage = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                        if(videoBoxGroup == null || !videoBoxGroup.updateMessageOverlay(overlayMessage)) {
                            sendMessageDelayed(Message.obtain(msg), 1000);
                        }
                        break;
                    case ON_SVC_REFRESH_LAYOUT_MODE:
                        if(videoBoxGroup != null && controller != null && videoBoxGroup.isRemoteCellReady()) {
                            controller.setLayoutMode(true);
                            AppSettings.getInstance().setSpeakerMode(true);
                        } else {
                            sendEmptyMessageDelayed(ON_SVC_REFRESH_LAYOUT_MODE, 1000);
                        }
                        break;
                    case ON_SVC_NETWORK_TOAST:
                        networkConditionToast.setVisibility(View.GONE);
                        break;
                    case ON_SVC_PEOPLE_NUMBER:
                        if(controller!=null){
                            LOG.info("send controller number");
                            controller.setNumber((String) msg.obj);
                        }
                        break;
                    case ON_SVC_MICROPHONEMUTED:
                        microphoneMuted.setVisibility(View.GONE);
                        break;
                    case ON_SVC_REMOTE_UPDATE_NAME :
                        String deviceId = msg.getData().getString("deviceId");
                        String displayName = msg.getData().getString("displayName");
                        boolean islocal = msg.getData().getBoolean("islocal",false);
                        LOG.info("islocal : "+islocal);
                        if(islocal && !displayName.equals("")){
                            videoBoxGroup.updateLocalName(displayName);
                        }
                        if(videoBoxGroup == null || !videoBoxGroup.updateRemoteName(deviceId,displayName)) {
                            sendMessageDelayed(Message.obtain(msg), 1000);
                        }
                        break;
                    default:
                        break;
                }

            }
        }
    };

    public void shareToWechat(final String json) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(json)) {
                    Utils.showToastWithCustomLayout(Conversation.this, getString(R.string.empty_share));
                    return;
                }
                try {
                    MeetingForWechat meeting = JsonUtil.toObject(json, MeetingForWechat.class);
                    WeChat.share(Conversation.this, meeting);
                } catch (Exception e) {
                    LOG.error("shareToWechat: "+ e.getMessage(), e);
                    Utils.showToastWithCustomLayout(Conversation.this, getString(R.string.share_failed));
                }
            }
        });
    }

    public void shareToEmail(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(content)) {
                    Utils.showToastWithCustomLayout(Conversation.this, getString(R.string.empty_share));
                    return;
                }
                try {
                    String[] email = {};
                    String uriString = "mailto:";
                    final String SUBJECT = "subject=";
                    String message = content.toString();
                    String emailBody = "";
                    if(message.startsWith(uriString) && message.contains("&body=")) {
                        String[] strs = message.split("&body=");
                        uriString = strs[0];
                        if(strs.length > 1) {
                            emailBody = strs[1];
                        }
                    }

                    String subject = getString(R.string.share_meeting);
                    if(message.indexOf(SUBJECT) > 0) {
                        subject = message.substring(message.indexOf(SUBJECT) + SUBJECT.length(),message.indexOf("&"));
                    }
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE, uri);
                    intent.putExtra(Intent.EXTRA_EMAIL, email); // 接收人
                    intent.putExtra(Intent.EXTRA_CC, ""); // 抄送人
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject); // 主题

                    String model = Build.BRAND;
                    if(model.equalsIgnoreCase("xiaomi")) {
                        emailBody = emailBody.replace("\n","<br>");
                        intent.setType("text/html");
                    }else {
                        intent.setType("text/plain");
                    }
                    intent.putExtra(Intent.EXTRA_TEXT, emailBody); // 正文
                    startActivity(Intent.createChooser(intent, getString(R.string.select_email_software)));
                } catch (Exception e) {
                    LOG.error("shareToWechat: "+ e.getMessage(), e);
                    Utils.showToast(Conversation.this, R.string.share_failed);
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        return retVal || super.onTouchEvent(event);
    }

    private void initGesture() {
        SVCGestureListener mGestureListener = new SVCGestureListener();
        mGestureDetector = new GestureDetector(this, mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(this, mGestureListener);
    }

    public class SVCGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(videoBoxGroup != null) {
                return videoBoxGroup.onScroll(e1, e2, distanceX, distanceY);
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(videoBoxGroup != null) {
                return videoBoxGroup.onScale(detector);
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
//            LOG.info("onSingleTapConfirmed : ["+e.getRawY()+"]");
            if(e.getRawY() > controller.getTopBarHeight()) {
                controller.toggleBar();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

  /*  AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener(){

        @Override
        public void onAudioFocusChange(int focusChange) {
            LOG.info("AudioManager "+focusChange);
        }
    };*/

    MeetingDialog  dialog;
    private String name;
    private void meetingDialog(int type){
          dialog = new MeetingDialog.Builder(Conversation.this).dialogType(type)
                .setCancelButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }).setInputWatcher(new TextWatcher() {
                      @Override
                      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                      @Override
                      public void onTextChanged(CharSequence s, int start, int before, int count) {}
                      @Override
                      public void afterTextChanged(Editable s) {
                          name = s.toString();
                      }
                  }).setOKButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogOkType(type);
                        dialog.dismiss();
                    }
                }).setEndButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HjtApp.getInstance().getAppService().onTerminateMeeting();
                        dialog.dismiss();
                    }
                }).createTwoButtonDialog();
        dialog.show();
    }

    private void dialogOkType(int type) {
        switch (type){
            case MeetingDialog.MEETING_UPDATE_NAME:
                String displayName = name;
                if(!displayName.equals("") && Utils.regExTest(displayName)){
                    LOG.info(" new displayName : "+displayName);
                    HjtApp.getInstance().getAppService().setConfDisplayName(displayName);
                    videoBoxGroup.updateLocalName(displayName);
                }else {
                    Utils.showToastWithCustomLayout(Conversation.this, getString(R.string.username_character,"”,<,>."));
                }
                break;
            case MeetingDialog.MEETING_UNMUTE:
                HjtApp.getInstance().getAppService().muteMic(false);
                if(controller != null) {
                    controller.muteMic(false);
                 }
                break;
            case MeetingDialog.MEETING_LEAVE:
                HjtApp.getInstance().getAppService().endCall();
                break;
            case MeetingDialog.MEETING_END:
                HjtApp.getInstance().getAppService().endCall();
                break;
            case MeetingDialog.MEETING_PERMISSION:
                PermissionUtil.GoToSetting(Conversation.this);
                break;

            default:
                break;
        }
    }


    public void floatWindowService(){
        Intent intent = new Intent(Conversation.this, MeetingWindowService.class);
        isBind = bindService(intent, connections, BIND_AUTO_CREATE | BIND_ABOVE_CLIENT);
    }

    private ServiceConnection connections = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOG.info("Bind MeetingWindowService: onServiceConnected");
            floatService = ((MeetingWindowService.FloatServiceBinder)service).getService();
            floatService.stopFloatWindow();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOG.info("Bind MeetingWindowService: onServiceDisconnected");
            floatService = null;
            floatWindowService();
        }
    };

    private void stopScreenCaptureService() {
        if(SystemCache.getInstance().isSharedScreen()){
            SystemCache.getInstance().setSharedScreen(false);
            Intent intent = new Intent(Conversation.this, ScreenCaptureService.class);
            stopService(intent);
        }
    }

}
