package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.FullscreenActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.RegisterState;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.Utils;
import com.hexmeet.hjt.widget.PulseView;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ConnectActivity extends FullscreenActivity {
    private Logger LOG = Logger.getLogger(this.getClass());
    private PulseView pulseView;

    boolean isVideoCall = true;
    private String sipNumber;
    private PasswordDialog dialog;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("onCreate");
        try {
            EventBus.getDefault().register(this);
            Bundle bundle = this.getIntent().getExtras();
            isVideoCall = bundle.getBoolean("isVideoCall", false);
            sipNumber = bundle.getString("sipNumber");

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        setContentView(R.layout.connect);

        TextView conn_to_text = (TextView) findViewById(R.id.conn_to_text);
        pulseView = (PulseView)findViewById(R.id.pulse_view);

        findViewById(R.id.end_call).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

        conn_to_text.setText(sipNumber);
        handler.sendEmptyMessageDelayed(1, 30 * 1000);
    }


    @Override
    protected void onStart() {
        super.onStart();
        ResourceUtils.getInstance().initScreenSize();
        startRinging();
        pulseView.startPulse();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRinging();
        pulseView.finishPulse();
    }

    @Override
    protected void onDestroy() {
        if(dialog != null && !dialog.isShowing()) {
            dialog.dismiss();
        }
        handler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void endCall() {
        HjtApp.getInstance().getAppService().endCall();
        ConnectActivity.this.finish();
        LOG.debug("hang up call successful");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            endCall();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("callevent : "+event.getCallState());
        if(event.getCallState() == CallState.CONNECTED) {
            handler.removeMessages(1);
            handler.sendEmptyMessageDelayed(0, 500);
        }

        if(event.getCallState() == CallState.IDLE) {
            handler.removeMessages(1);
            if(!TextUtils.isEmpty(event.getEndReason())) {
                Utils.showToast(ConnectActivity.this, event.getEndReason());
            }
            finish();
        }

        if(event.getCallState() == CallState.AUTHORIZATION){
            handler.removeMessages(1);
            handler.sendEmptyMessageDelayed(1, 30 * 1000);
            showPasswordDialog();
        }
    }

    private void showPasswordDialog() {
        if(dialog == null) {
            dialog = new PasswordDialog.Builder(ConnectActivity.this)
                    .setInputWatcher(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            password = s.toString();
                        }
                    })
                    .setNegativeButton(null, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            EventBus.getDefault().post(new CallEvent(CallState.IDLE));
                        }
                    })
                    .setPositiveButton(null, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(TextUtils.isEmpty(password)) {
                                Utils.showToast(ConnectActivity.this, R.string.input_call_password);
                            } else {
                                dialog.dismiss();
                                HjtApp.getInstance().getAppService().makeCall(sipNumber, password);
                            }

                        }
                    }).createTwoButtonDialog();
        }
        dialog.show();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                if(SystemCache.getInstance().getPeer() != null) {
                    Intent intent = new Intent();
                    intent.setClass(ConnectActivity.this, Conversation.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    sendEmptyMessageDelayed(0, 1000);
                }
            } else if (msg.what == 1) {
                HjtApp.getInstance().getAppService().endCall();
                EventBus.getDefault().post(new CallEvent(CallState.IDLE));
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegisterEvent(RegisterState state) {
        if (state == RegisterState.SUCCESS && SystemCache.getInstance().isConnecting()) {
            handler.removeMessages(1);
            handler.sendEmptyMessageDelayed(1, 30 * 1000);
            String passwords = SystemCache.getInstance().getPeer().getPassword();
            String number = SystemCache.getInstance().getPeer().getNumber();
            HjtApp.getInstance().getAppService().makeCall(number,passwords);
        }
    }

}
