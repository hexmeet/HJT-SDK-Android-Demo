package com.hexmeet.hjt.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.LinearLayout;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.Conversation;

import org.apache.log4j.Logger;

public class MeetingWindowService extends Service {
    private Logger LOG = Logger.getLogger(MeetingWindowService.class);
    private final static int FLOAT_NOTIFICATION_ID = 11;
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        LOG.info("MeetingWindowService onCreate");
       // HjtApp.getInstance().setFloatServiceStart(true);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotification();
            }
            /**/
            boolean permissionOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(HjtApp.getInstance().getContext());
            if(permissionOk) {
                if(!HjtApp.getInstance().isFloatServiceStart()) {
                    return;
                }
               if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(HjtApp.getInstance().getContext())) {
                    LOG.info("onStop start to show float window");
                    createFloatView();
                } else {
                    LOG.info("onStop will not show float window");
                   // Utils.showToast(getApplicationContext(), R.string.need_float_window_permission);
                }

                hasWindow = true;
            } else {
                isLoadComplete = true;
                hasWindow = false;
            }
        } catch (Exception e) {
            LOG.error("MeetingWindowService onCreate", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFloatView() {
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
        //wmParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

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
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        chronometer = mFloatLayout.findViewById(R.id.timer_chronometer);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        //浮动窗口按钮
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动

        width = mFloatLayout.getMeasuredWidth() / 2;
        height = mFloatLayout.getMeasuredHeight() / 2;
        isLoadComplete = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.info("MeetingWindowService onStartCommand");
        if(!isLoadComplete || !hasWindow ) {
            return super.onStartCommand(intent, flags, startId);
        }
        mFloatLayout.setOnTouchListener(touchListener);
        mFloatLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isMoved) {
                    return;
                }
                Intent i = new Intent(MeetingWindowService.this, Conversation.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        if(SystemCache.getInstance().getPeer() != null) {
            chronometer.setBase(SystemCache.getInstance().getStartTime());
            LOG.info("Meeting Start Time =  " + SystemCache.getInstance().getStartTime());

            chronometer.start();
        }
        return START_STICKY;
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isMoved = false;
                downX = x;
                downY = y;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (isMoved || Math.abs(x - downX) > touchSlop || Math.abs(y - downY) > touchSlop) {
                    if (!isMoved) isMoved = true;
                    wmParams.x = x - width;
                    wmParams.y = y - height;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
            }
            return false;
        }
    };

    @Override
    public void onDestroy() {
        LOG.info("MeetingWindowService onDestroy");
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
       // HjtApp.getInstance().setFloatServiceStart(false);
        isLoadComplete = false;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {
    Bitmap btm = BitmapFactory.decodeResource(getResources(), R.drawable.notification_icon);
       Intent intent = new Intent(this, Conversation.class);
       intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
       NotificationChannel channel = new NotificationChannel("id","name", NotificationManager.IMPORTANCE_HIGH);
       NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       manager.createNotificationChannel(channel);
       Notification notification = new Notification.Builder(this,"id")
               .setLargeIcon(btm)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(HjtApp.getInstance().getString(R.string.meeting_progress))
                .setContentText(HjtApp.getInstance().getString(R.string.meeting_click))
                .setContentIntent(PendingIntent.getActivity(this, FLOAT_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        notification.priority = Notification.PRIORITY_HIGH;
        startForeground(FLOAT_NOTIFICATION_ID, notification);
       // manager.notify(FLOAT_NOTIFICATION_ID,notification);
    }
}


