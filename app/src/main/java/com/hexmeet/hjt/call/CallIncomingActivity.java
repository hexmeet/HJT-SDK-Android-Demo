package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.PermissionWrapper;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.sdk.Peer;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CallIncomingActivity extends Activity {
    private Logger LOG = Logger.getLogger(this.getClass());
    private LinearLayout btnLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        setFinishOnTouchOutside(false);
        this.setContentView(R.layout.call_incoming);


        Peer peer = SystemCache.getInstance().getPeer();
        btnLayout = findViewById(R.id.bottom_btn);
        if (peer != null) {
            TextView title = findViewById(R.id.conference_join);
            if(TextUtils.isEmpty(peer.getFrom())) {
                title.setText(getString(R.string.invite_conference, peer.getName()));
            } else {
                title.setText(getString(R.string.invite_conference_from, peer.getFrom(), peer.getName()));
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
        if (AppSettings.getInstance().isAutoAnswer()) {
            btnLayout.setVisibility(View.GONE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Peer peer = SystemCache.getInstance().getPeer();
                    HjtApp.getInstance().getAppService().answerCall(peer.getNumber(), peer.getPassword());
                }
            }, 2000);
        } else {
            btnLayout.getChildAt(0).setOnClickListener(clickListener);
            btnLayout.getChildAt(1).setOnClickListener(clickListener);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new CallEvent(CallState.IDLE));
                finish();
            }
        }, 60000);
    }


    @Override
    protected void onStart() {
        super.onStart();
        ResourceUtils.getInstance().initScreenSize();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_hangup) {
                handler.removeCallbacksAndMessages(null);
                EventBus.getDefault().post(new CallEvent(CallState.IDLE));
                finish();
            } else {
                Peer peer = SystemCache.getInstance().getPeer();
                HjtApp.getInstance().getAppService().answerCall(peer.getNumber(), peer.getPassword());
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
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        if (event.getCallState() == CallState.CONNECTED) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }

        if (event.getCallState() == CallState.IDLE) {
            if(!TextUtils.isEmpty(event.getEndReason())) {
                Utils.showToast(CallIncomingActivity.this, event.getEndReason());
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
}
