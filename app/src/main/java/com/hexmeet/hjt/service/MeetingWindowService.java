package com.hexmeet.hjt.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.call.RemoteBox;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.MicMuteUpdateEvent;
import com.hexmeet.hjt.event.RemoteNameEvent;
import com.hexmeet.hjt.event.SvcSpeakerEvent;
import com.hexmeet.hjt.sdk.SvcLayoutInfo;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class MeetingWindowService extends Service {
    private Logger LOG = Logger.getLogger(MeetingWindowService.class);

    private LinearLayout mFloatLayout;
    private Chronometer chronometer;
    private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;

    private boolean isMoved = false;
    private int downX = -1;
    private int downY = -1;
    private int touchSlop;

    private int width = -1;
    private int height = -1;
    private boolean isLoadComplete = false;
    private boolean hasWindow = false;

    private RemoteBox remoteBox;
    private boolean remoteCellReady = false;
    private final static int ON_SVC_LAYOUT_CHANGED = 11;
    private final static int ON_SVC_SPEAKER_CHANGED = 12;
    private final static int ON_SVC_MIC_MUTE_CHANGED = 13;
    private final static int ON_SVC_REMOTE_UPDATE_NAME = 19;
    public final static int ON_SVC_FLOAT_WINDOW = 20;

    private RelativeLayout.LayoutParams  fullScreenLayoutPara;
    private final static int FLOAT_NOTIFICATION_ID = 111;


    private ImageView timericon;
    private RelativeLayout video_surface_view;
    private FloatServiceBinder mBinder = new FloatServiceBinder();
    private LayoutInflater inflater;
    private boolean isRegisterEventBus = false ;
    private boolean isAddWindowManager = false;

    public class FloatServiceBinder extends Binder {
        public MeetingWindowService getService() {
            return MeetingWindowService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        LOG.info("MeetingWindowService onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotification();
        }
        startFloatWindow();
    }

    public void startFloatWindow(){
        try {
            boolean permissionOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(HjtApp.getInstance().getContext());
            if(permissionOk) {
                LOG.info("onStart  to show float window");
                initWindow();
                hasWindow = true;
            } else {
                LOG.info("onStop will not show float window");
                isLoadComplete = true;
                hasWindow = false;
              //  Utils.showToast(getApplicationContext(), R.string.need_float_window_permission);
            }
        } catch (Exception e) {
            LOG.error("MeetingWindowService onCreate", e);
        }
    }

   private void initWindow(){
       LOG.info("initWindow()");
       wmParams = new WindowManager.LayoutParams();
       //获取的是WindowManagerImpl.CompatModeWrapper
       mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
       } else {
           wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
       }
       //设置图片格式，效果为背景透明
       wmParams.format = PixelFormat.RGBA_8888;
       //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
       wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
       //调整悬浮窗显示的停靠位置为左侧置顶
       wmParams.gravity = Gravity.LEFT | Gravity.TOP;
       // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
       wmParams.x = 0;
       wmParams.y = 0;

       //设置悬浮窗口长宽数据
       wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
       wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		 /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/
       touchSlop = ViewConfiguration.get(getApplicationContext()).getScaledTouchSlop();
       fullScreenLayoutPara = new RelativeLayout.LayoutParams(ResourceUtils.screenWidth, ResourceUtils.screenHeight);
       fullScreenLayoutPara.addRule(RelativeLayout.CENTER_IN_PARENT);
       inflater = LayoutInflater.from(getApplication());
       //获取浮动窗口视图所在布局
       mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
       chronometer = mFloatLayout.findViewById(R.id.timer_chronometer);
       timericon = (ImageView) mFloatLayout.findViewById(R.id.timer_icon);
       video_surface_view = (RelativeLayout) mFloatLayout.findViewById(R.id.video_surface_view);
       mFloatLayout.setVisibility(View.GONE);
       //添加mFloatLayout
       mWindowManager.addView(mFloatLayout, wmParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void createFloatView() {
        if(!hasWindow){
            Utils.showToast(HjtApp.getInstance().getContext(),getString(R.string.need_float_window_permission,getString(R.string.app_name)));
            return;
        }

        if(!isRegisterEventBus){
            isRegisterEventBus = true;
            EventBus.getDefault().register(this);
        }
        LOG.info("createFloatView()");

        mFloatLayout.setVisibility(View.VISIBLE);
        if(SystemCache.getInstance().isUserVideoMode()){//true 视频 false 音频
            chronometer.setVisibility(View.GONE);
            timericon.setVisibility(View.GONE);
            remoteBox = new RemoteBox(video_surface_view.getContext(),true);
            remoteBox.setSvcListener(new RemoteBox.SvcSurfaceListener() {
                @Override
                public void onAllSurfaceReady() {
                    remoteCellReady = true;
                    HjtApp.getInstance().getAppService().setRemoteViewToSdk(remoteBox.getAllSurfaces());
                }
            });
            remoteBox.setShowContent(false);
            onSvcLayoutChangedEvent(SystemCache.getInstance().getSvcLayoutInfo());
            video_surface_view.addView(remoteBox,0,fullScreenLayoutPara);
        }else {
            LOG.info("Audio Mode");
            chronometer.setVisibility(View.VISIBLE);
            timericon.setVisibility(View.VISIBLE);
            video_surface_view.setVisibility(View.GONE);
        }

        isAddWindowManager = true;
        //浮动窗口按钮
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        width = mFloatLayout.getMeasuredWidth() / 2;
        height = mFloatLayout.getMeasuredHeight() / 2;
        isLoadComplete = true;
        onClick();
    }

    private void onClick() {
        mFloatLayout.setOnTouchListener(touchListener);
        mFloatLayout.setOnClickListener(new View.OnClickListener( ) {

            @Override
            public void onClick(View v) {
                LOG.info("floutLayout onclick "+isMoved);
                if (isMoved) {
                    return;
                }
                Intent i = new Intent(MeetingWindowService.this, Conversation.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        if(!SystemCache.getInstance().isUserVideoMode() && SystemCache.getInstance().getPeer() != null) {
            chronometer.setBase(SystemCache.getInstance().getStartTime());
            LOG.info("Meeting Start Time =  " + SystemCache.getInstance().getStartTime());
            chronometer.start();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.info("MeetingWindowService onStartCommand");
        if(!isLoadComplete || !hasWindow ) {
            return super.onStartCommand(intent, flags, startId);
        }
        return START_STICKY;
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            LOG.info("floutLayout touchListener ");
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isMoved = false;
                downX = x;
                downY = y;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE && (isMoved || Math.abs(x - downX) > touchSlop || Math.abs(y - downY) > touchSlop)) {
                if (!isMoved) isMoved = true;
                wmParams.x = x - width;
                wmParams.y = y - height;
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
            }
            return false;
        }
    };

   public void stopFloatWindow(){
       LOG.info("stopFloatWindow() , isRegisterEventBus : "+isRegisterEventBus);
       if(isRegisterEventBus){
           isRegisterEventBus = false;
           EventBus.getDefault().unregister(this);
       }

       if (mWindowManager!=null && mFloatLayout != null && isAddWindowManager){
           LOG.info("mWindowManager!=null ");
           isAddWindowManager = false;
           mFloatLayout.setVisibility(View.GONE);
       }
       if(remoteBox!=null){
           LOG.info("float window release remotebox ");
           remoteBox.release();
       }
       svcHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        LOG.info("MeetingWindowService onDestroy");
        super.onDestroy();
        isLoadComplete = false;
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        if (mWindowManager!=null && mFloatLayout != null && isAddWindowManager){
            mFloatLayout.setVisibility(View.GONE);
            mWindowManager.removeViewImmediate(mFloatLayout);// 同步删除, mWindowManager.removeView(mFloatLayout) 异步删除;
        }
    }
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSvcLayoutChangedEvent(SvcLayoutInfo info) {
        svcHandler.removeMessages(ON_SVC_LAYOUT_CHANGED);
        Message msg = Message.obtain();
        msg.what = ON_SVC_LAYOUT_CHANGED;
        msg.getData().putParcelable(AppCons.BundleKeys.EXTRA_DATA, info);
        svcHandler.sendMessage(msg);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSvcSpeakerChangedEvent(SvcSpeakerEvent event) {
        LOG.info("SvcSpeakerEvent : "+event.getSiteName());
        svcHandler.removeMessages(ON_SVC_SPEAKER_CHANGED);
        if(!SystemCache.getInstance().isUserVideoMode()){
            if(!event.getSiteName().equals("")){
                //Utils.showToastWithCustomLayout(this, String.format(event.getSiteName(), getString(R.string.app_name)));
            }
            return;
        }
        Message msg = Message.obtain();
        msg.what = ON_SVC_SPEAKER_CHANGED;
        msg.arg1 = event.getIndex();
        msg.obj = event.getSiteName();
        svcHandler.sendMessage(msg);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRemoteNameEvent(RemoteNameEvent event) {
        svcHandler.removeMessages(ON_SVC_REMOTE_UPDATE_NAME);
        LOG.info("RemoteNameEvent : "+event.isLocal());
        if(event != null && !event.isLocal()) {
            Message msg = Message.obtain();
            msg.what = ON_SVC_REMOTE_UPDATE_NAME;
            Bundle bundle = new Bundle();
            bundle.putString("deviceId",event.getDeviceId());
            bundle.putString("displayName",event.getName());
            msg.setData(bundle);
            svcHandler.sendMessageDelayed(msg, 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRemoteMicMuteEvent(MicMuteUpdateEvent event) {
        Message msg = Message.obtain();
        msg.what = ON_SVC_MIC_MUTE_CHANGED;
        msg.getData().putString("participants", event.getParticipants());
        svcHandler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    public Handler svcHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ON_SVC_LAYOUT_CHANGED:
                    SvcLayoutInfo svcLayoutInfo = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                    if(svcLayoutInfo !=null){
                        LOG.info("SvcLayoutInfo: " + svcLayoutInfo.toString());
                        if (remoteBox != null && remoteCellReady) {
                            remoteBox.updateLayout(svcLayoutInfo);
                        }
                    }
                    break;
                case ON_SVC_SPEAKER_CHANGED:
                    if (remoteBox !=null){
                        remoteBox.updateSpeaker(msg.arg1, (String) msg.obj);
                    }
                    break;
                case ON_SVC_MIC_MUTE_CHANGED:
                    if (remoteBox !=null){
                        remoteBox.updateMicMute(msg.getData().getString("participants"));
                    }
                    break;
                case ON_SVC_REMOTE_UPDATE_NAME :
                    String deviceId = msg.getData().getString("deviceId");
                    String displayName = msg.getData().getString("displayName");
                    if(remoteBox != null) {
                        remoteBox.updateRemoteName(deviceId,displayName);
                    }
                    break;
                case ON_SVC_FLOAT_WINDOW:
                    LOG.info("hander startFloatWindow()");
                    createFloatView();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {
        Bitmap btm = BitmapFactory.decodeResource(getResources(), R.drawable.notification_icon);
        Intent intent = new Intent(this, Conversation.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification notification = new Notification.Builder(this, BuildConfig.FLOATNOTIFICATION)
                .setLargeIcon(btm)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(HjtApp.getInstance().getString(R.string.meeting_progress))
                .setContentText(HjtApp.getInstance().getString(R.string.meeting_click))
                .setContentIntent(PendingIntent.getActivity(this, FLOAT_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        notification.priority = Notification.PRIORITY_HIGH;
        startForeground(FLOAT_NOTIFICATION_ID, notification);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("event : "+event.getCallState());
        if (event.getCallState() == CallState.IDLE) {
            AppSettings.getInstance().setSpeakerMode(false);
            stopSelf();
        }
    }
}


