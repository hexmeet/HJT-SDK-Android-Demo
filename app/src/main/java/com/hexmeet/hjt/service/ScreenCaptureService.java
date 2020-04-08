package com.hexmeet.hjt.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.ContentEvent;
import com.hexmeet.hjt.utils.ResourceUtils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ScreenCaptureService extends Service {
    private  Logger LOG = Logger.getLogger(ScreenCaptureService.class);

    private  LinearLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private LayoutInflater inflater;
    private boolean isMoved = false;
    private int downX = -1;
    private int downY = -1;
    private int touchSlop;

    private int width = -1;
    private int height = -1;
    private final static int FLOAT_NOTIFICATION_ID = 111;

    private RelativeLayout.LayoutParams  fullScreenLayoutPara;
    private MediaProjection mediaProjection;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        LOG.info("onCreate()");
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotification();
        }
        initWindow();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.info("onStartCommand()");
        if(intent != null) {
            String mResultCode = intent.getStringExtra("code");
            Intent mResultData = intent.getParcelableExtra("data");
            if((mResultCode!=null && !mResultCode.equals("")) && mResultData!=null){
                MediaProjectionManager mProjectionManager = (MediaProjectionManager) getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
                mediaProjection = mProjectionManager.getMediaProjection(Integer.parseInt(mResultCode), Objects.requireNonNull(mResultData));
                onScreenCapturer(mediaProjection);
                return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initWindow(){
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
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.screen_capture_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatLayout.setVisibility(View.GONE);
        //浮动窗口按钮
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        width = mFloatLayout.getMeasuredWidth() / 2;
        height = mFloatLayout.getMeasuredHeight() / 2;

        mFloatLayout.setOnTouchListener(touchListener);
        mFloatLayout.setOnClickListener(new View.OnClickListener( ) {

            @Override
            public void onClick(View v) {
                LOG.info("screen capture onclick "+isMoved);
                if (isMoved) {
                    return;
                }
                closeService();
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.info("onDestroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }

        if (mWindowManager!=null && mFloatLayout != null ){
            mWindowManager.removeViewImmediate(mFloatLayout);//mWindowManager.removeView(mFloatLayout);
        }
        mHandler.removeCallbacksAndMessages(null);
        HjtApp.getInstance().getAppService().stopShare();
        EventBus.getDefault().unregister(this);
    }

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

    public  void onScreenCapturer(MediaProjection mediaProjection) {
        mFloatLayout.setVisibility(View.VISIBLE);
        Display mDisplay = mWindowManager.getDefaultDisplay();
        mHandler = new Handler();
        HjtApp.getInstance().getAppService().startScreenShare(ScreenCaptureService.this,mediaProjection,mDisplay, mHandler);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Configuration cf= this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = cf.orientation ; //获取屏幕方向
        LOG.info("ori : "+ori);
        if(ori == cf.ORIENTATION_LANDSCAPE){//横屏
            HjtApp.getInstance().getAppService().setDirection(true);
        }else if(ori == cf.ORIENTATION_PORTRAIT){//竖屏
            HjtApp.getInstance().getAppService().setDirection(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(ContentEvent event) {
        if(event.withContent()){
            closeService();
        }
    }

    private void closeService(){
        SystemCache.getInstance().setSharedScreen(false);
        Intent i = new Intent(ScreenCaptureService.this, Conversation.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        HjtApp.getInstance().getAppService().stopShare();
        EventBus.getDefault().post(SharedState.STOPSCREENSHARE);
        stopSelf();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("event : "+event.getCallState());
        if (event.getCallState() == CallState.IDLE) {
            HjtApp.getInstance().getAppService().stopShare();
            stopSelf();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void SharedStateEvent(SharedState event) {
        LOG.info("ContentDeclinedEvent :" + event);
        if (event==SharedState.NOPERMISSION) {
            SystemCache.getInstance().setSharedPermission(true);
            closeService();
        }
    }

}
