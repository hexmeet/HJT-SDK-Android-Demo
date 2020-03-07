package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.FullscreenActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.PermissionWrapper;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.FileMessageEvent;
import com.hexmeet.hjt.sdk.Peer;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.widget.PulseView;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;

public class CallIncomingActivity  extends FullscreenActivity {
    private Logger LOG = Logger.getLogger(this.getClass());
    private LinearLayout btnLayout;
    private PulseView pulseView;
    private LinearLayout btn_hangup;
    private LinearLayout btn_video;
    private TextView message;
    private TextView title;
    private ImageView avatar;
    private Peer peer;

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("onCreate()");
        EventBus.getDefault().register(this);
        this.setContentView(R.layout.call_incoming);
        pulseView = (PulseView)findViewById(R.id.conference_pulse_view);
        btn_hangup = (LinearLayout) findViewById(R.id.btn_hangup);
        btn_video = (LinearLayout) findViewById(R.id.btn_video);
        avatar = (ImageView)findViewById(R.id.called_avatar);
        btnLayout = findViewById(R.id.bottom_btn);
        title = findViewById(R.id.conference_join);
        message = (TextView)findViewById(R.id.conference_from);

        peer = SystemCache.getInstance().getPeer();
        if(peer == null) {
            finish();
            return;
        }
        LOG.info("peer : "+ peer.toString());

        if (peer != null) {
            if(!peer.isP2P()){
                title.setText(peer.getFrom());
                if(TextUtils.isEmpty(peer.getFrom())) {
                    message.setText(getString(R.string.invite_conference, peer.getNumber()));
                } else {
                    message.setText(getString(R.string.invite_conference_from, peer.getNumber()));
                }
            }else {
                avatar.setVisibility(View.VISIBLE);
                title.setText(peer.getName());
                avaterUrl(peer.getImageUrl());
                message.setText(getString(R.string.call_invited));
            }

        }

        if(PermissionWrapper.getInstance().checkMeetingPermission(CallIncomingActivity.this)) {
            handleCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            handleCall();
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
            EventBus.getDefault().post(new CallEvent(CallState.IDLE));
            handler.removeCallbacksAndMessages(null);
            finish();
        }
    }

    private void handleCall() {
        btn_hangup.setOnClickListener(clickListener);
        LOG.info("isAutoAnswer : "+AppSettings.getInstance().isAutoAnswer());
        if (AppSettings.getInstance().isAutoAnswer()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn_video.setVisibility(View.GONE);
                    if(!peer.isP2P()){
                        message.setVisibility(View.GONE);
                        title.setText(peer.getNumber());
                        HjtApp.getInstance().getAppService().answerCall(peer.getNumber(), peer.getPassword());
                    }else {
                        HjtApp.getInstance().getAppService().makeCall(peer.getNumber(), peer.getPassword(),true);
                    }

                }
            }, 200);
        }else {
            btn_video.setVisibility(View.VISIBLE);
            btn_video.setOnClickListener(clickListener);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        LOG.info("onStart()");
        startRinging();
        ResourceUtils.getInstance().initScreenSize();
        pulseView.startPulse();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LOG.info("onStop()");
        stopRinging();
        pulseView.finishPulse();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_hangup) {
                LOG.info("onClick hangup");
                handler.removeMessages(0);
                HjtApp.getInstance().getAppService().refuseP2PMetting(SystemCache.getInstance().getPeer().getNumber());
                handler.removeCallbacksAndMessages(null);
                EventBus.getDefault().post(new CallEvent(CallState.IDLE));
                finish();
            } else {
                LOG.info("onClick call meeting");
                btn_video.setVisibility(View.GONE);
                if(!peer.isP2P()){
                    message.setVisibility(View.GONE);
                    title.setText(peer.getNumber());
                    HjtApp.getInstance().getAppService().answerCall(peer.getNumber(), peer.getPassword());
                }else {
                    HjtApp.getInstance().getAppService().makeCall(peer.getNumber(), peer.getPassword(),true);
                }



            }
        }
    };

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
        LOG.info("onDestroy()");
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
            if(!TextUtils.isEmpty(event.getEndReason())) {
                Toast.makeText(CallIncomingActivity.this,event.getEndReason(),Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
               if (SystemCache.getInstance().getPeer() != null) {
                   if(peer.isP2P()){
                       SystemCache.getInstance().getPeer().setName("");
                   }
                    Intent intent = new Intent();
                    intent.setClass(CallIncomingActivity.this, Conversation.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    sendEmptyMessageDelayed(0, 1000);
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFileMessageEven(FileMessageEvent event) {//获取p2p主叫头像
        LOG.info("onFileMessageEven()");
        avaterUrl(event.getFilePath());
    }

    private void avaterUrl(String imageUrl) {
        Glide.with(this).load(imageUrl).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(avatar);
    }
}
