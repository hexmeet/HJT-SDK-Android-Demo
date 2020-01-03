package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.FullscreenActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.PermissionWrapper;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.sdk.Peer;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.widget.PulseView;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import ev.common.EVEngine;
import ev.common.EVFactory;

public class P2pCallActivity extends FullscreenActivity {
    private Logger LOG = Logger.getLogger(this.getClass());
    private ImageView mCallerAvatar;
    private TextView mCallerName;
    private LinearLayout mCallerEnd;
    private SurfaceView surfaceView;
    private RelativeLayout dial_notify_View;
    private SurfaceView mDummyPreviewView; // this surface is used to capture image from camera
    private Peer peer;
    private OrientationEventListener orientationListener;
    private HandlerThread handlerThread = new HandlerThread("dialInNotify_worker");
    private PulseView mPulseViewAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("onCreate");
        setFinishOnTouchOutside(false);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_p2p_call);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initView();
        initData();
    }

    private void initView() {
        mCallerAvatar = (ImageView) findViewById(R.id.caller_avatar);
        mPulseViewAvatar = (PulseView) findViewById(R.id.pulse_view_avatar);
        mCallerName = (TextView) findViewById(R.id.caller_name);
        mCallerEnd = (LinearLayout) findViewById(R.id.caller_end);

        peer = SystemCache.getInstance().getPeer();
        if (peer != null) {
            LOG.info("peer message : "+peer.getName());
            mCallerName.setText(peer.getName());
            Glide.with(this).load(peer.getImageUrl()).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mCallerAvatar);

            if (peer.isVideoCall()) {
                mDummyPreviewView = (SurfaceView) findViewById(R.id.previewView);
                mDummyPreviewView.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        LOG.info("mDummyPreviewView display surface is being changed. format: " + format
                                + ", width: " + width + ", height: " + height + ", surface: " + holder.getSurface());
                    }

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        LOG.info("mDummyPreviewView display surface created");
                        HjtApp.getInstance().getAppService().setPreviewToSdk(mDummyPreviewView);

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {

                        LOG.info("mDummyPreviewView display surface destroyed");
                    }
                });
                setOrientation();//这是本地视频方向
                setLocalBox();//开启本地视频
            }
        }


        if (PermissionWrapper.getInstance().checkMeetingPermission(P2pCallActivity.this)) {
            handleCall();
        }

    }

    private void initData() {
        mCallerEnd.setOnClickListener(clickListener);
    }

    private void handleCall() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new CallEvent(CallState.IDLE));
                finish();
            }
        }, 60000);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.caller_end) {
                handler.removeCallbacksAndMessages(null);
                HjtApp.getInstance().getAppService().endCall();
                finish();
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        startRinging();
        mPulseViewAvatar.startPulse();
        ResourceUtils.getInstance().initScreenSize();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRinging();
        mPulseViewAvatar.finishPulse();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("CallEvent : " + event.getCallState());
        if (event.getCallState() == CallState.CONNECTED) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }

        if (event.getCallState() == CallState.IDLE) {
            if (!TextUtils.isEmpty(event.getEndReason())) {
                //Utils.showToast(P2pCallActivity.this, event.getEndReason());
                Toast.makeText(P2pCallActivity.this,event.getEndReason(),Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
            handler.removeCallbacksAndMessages(null);
            finish();
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (SystemCache.getInstance().getPeer() != null) {
                    SystemCache.getInstance().getPeer().setName("");
                    Intent intent = new Intent();
                    intent.setClass(P2pCallActivity.this, Conversation.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    HjtApp.getInstance().getAppService().setPreviewToSdk(null);
                    HjtApp.getInstance().getAppService().setLocalViewToSdk(null);
                    finish();
                } else {
                    sendEmptyMessageDelayed(0, 1000);
                }
            }
        }
    };


    private void setLocalBox() {
        dial_notify_View = (RelativeLayout) findViewById(R.id.dial_notify_View);
        surfaceView = EVFactory.createWindow(dial_notify_View.getContext(), EVEngine.WindowType.LocalVideoWindow);
        surfaceView.setId(ResourceUtils.generateViewId());
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LOG.info("mCallingView display surface is being changed. format: " + format + ", width: "
                        + width + ", height: " + height + ", surface: " + holder.getSurface());
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LOG.info("mCallingView display surface created");
                HjtApp.getInstance().getAppService().setLocalViewToSdk(surfaceView);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                LOG.info("mCallingView display surface destroyed");
            }
        });
        dial_notify_View.addView(surfaceView, 0);

    }

    private void setOrientation() {
        if (orientationListener == null) {

            orientationListener = new OrientationEventListener(P2pCallActivity.this) {
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
    }

}