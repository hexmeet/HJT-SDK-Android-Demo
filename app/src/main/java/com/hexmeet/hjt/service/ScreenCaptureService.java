package com.hexmeet.hjt.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.util.DisplayMetrics;
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
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.utils.ResourceUtils;

import org.apache.log4j.Logger;

import java.util.Objects;

import androidx.annotation.NonNull;
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
    private  MediaProjection sMediaProjection;
    private  Handler mHandler;
    private  Display mDisplay;
    private  int mDensity;
    private  int mWidth;
    private  int mHeight;
    private  ImageReader mImageReader;
    private  VirtualDisplay mVirtualDisplay;


    @Override
    public void onCreate() {
        super.onCreate();
        LOG.info("onCreate()");
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

        int mResultCode = intent.getIntExtra("code", -1);
        Intent mResultData = intent.getParcelableExtra("data");

        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        MediaProjection mediaProjection = mProjectionManager.getMediaProjection(mResultCode, Objects.requireNonNull(mResultData));
        ScreenCapturer(mediaProjection);

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
                Intent i = new Intent(ScreenCaptureService.this, Conversation.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                SystemCache.getInstance().setSharedScreen(false);
                stopProjection();
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
        stopProjection();
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

    public  void ScreenCapturer(MediaProjection mediaProjection) {
        mFloatLayout.setVisibility(View.VISIBLE);

        sMediaProjection = mediaProjection;
        mHandler = new Handler();
        mDisplay = mWindowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getRealMetrics(metrics);
        mDensity = metrics.densityDpi;
        LOG.info("metrics.widthPixels is " + metrics.widthPixels);
        LOG.info("metrics.heightPixels is " + metrics.heightPixels);
        mWidth = metrics.widthPixels;//size.x;
        mHeight = metrics.heightPixels;//size.y;

        //start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(
                "ScreenShot",
                mWidth,
                mHeight,
                mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(),
                null,
                mHandler);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

            }
        }, mHandler);
        sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
    }

    private  class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            LOG.info("media projecation stop()");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                    }
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }

    }

    public  void stopProjection() {
        LOG.info("Screen captured stopProjection");
        if (sMediaProjection != null) {
            LOG.info("stop()");
            sMediaProjection.stop();
        }

    }

}
