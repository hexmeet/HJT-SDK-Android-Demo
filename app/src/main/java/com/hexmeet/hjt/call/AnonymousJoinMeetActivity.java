package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import com.hexmeet.hjt.PermissionWrapper;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.login.JoinMeetingParam;
import com.hexmeet.hjt.login.LoginService;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.Utils;
import com.hexmeet.hjt.widget.PulseView;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class AnonymousJoinMeetActivity extends FullscreenActivity {
    private Logger LOG = Logger.getLogger(this.getClass());
    private PulseView pulseView;
    private TextView conn_to_text;
    private View endCallBtn;
    private JoinMeetingParam param;
    private PasswordDialog dialog;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        param = SystemCache.getInstance().getJoinMeetingParam();

        if(param == null) {
            finish();
            return;
        }

        HjtApp.getInstance().getAppService().setUserInLogin(false);

        setContentView(R.layout.connect);
        conn_to_text = (TextView) findViewById(R.id.conn_to_text);
        endCallBtn = findViewById(R.id.end_call);
        pulseView = (PulseView)findViewById(R.id.pulse_view);

        setupEvent();

        conn_to_text.setText(param.getConferenceNumber());
        if(PermissionWrapper.getInstance().checkMeetingPermission(AnonymousJoinMeetActivity.this)) {
            LOG.info("CALL PROCESSING - start Anonymous Call");
            SystemCache.getInstance().setAnonymousMakeCall(true);
            LoginService.getInstance().anonymousMakeCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            SystemCache.getInstance().setAnonymousMakeCall(true);
            LoginService.getInstance().anonymousMakeCall();
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
            handler.removeCallbacksAndMessages(null);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRinging();
        ResourceUtils.getInstance().initScreenSize();
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

    public static void gotoAnonymousMeeting(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, AnonymousJoinMeetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void setupEvent() {
        LOG.debug("endcall_btn setup listener event");
        endCallBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                endCall();
            }
        });
    }

    protected void endCall() {
        HjtApp.getInstance().getAppService().endCall();
        LOG.debug("hang up call successful");
        if(SystemCache.getInstance().isInviteMakeCall()){
            clearInviteData();
        }
        handler.sendEmptyMessageDelayed(2, 500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(endCallBtn.isEnabled()) {
                endCall();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("AnonymousJoinMeet : "+event.getCallState());
        if(event.getCallState() == CallState.CONNECTED && SystemCache.getInstance().isAnonymousMakeCall()) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }

        if(event.getCallState() == CallState.IDLE) {
            if(!TextUtils.isEmpty(event.getEndReason())) {
                Utils.showToast(AnonymousJoinMeetActivity.this, event.getEndReason());
            }
            handler.sendEmptyMessageDelayed(1, 1500);
        }

        if(event.getCallState() == CallState.AUTHORIZATION && SystemCache.getInstance().isAnonymousMakeCall()){
            showPasswordDialog();
        }
    }

    private void showPasswordDialog() {
        if(dialog == null) {
            dialog = new PasswordDialog.Builder(AnonymousJoinMeetActivity.this)
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
                                Utils.showToast(AnonymousJoinMeetActivity.this, R.string.input_call_password);
                            } else {
                                dialog.dismiss();
                                param.setPassword(password);
                                SystemCache.getInstance().getJoinMeetingParam().setPassword(password);
                                LoginService.getInstance().anonymousMakeCall();
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
                if (!SystemCache.getInstance().isAnonymousMakeCall()) {
                    clearAnonymousData();
                    finish();
                    return;
                }
                if(SystemCache.getInstance().getPeer() != null) {
                    Intent intent = new Intent();
                    intent.setClass(AnonymousJoinMeetActivity.this, Conversation.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    sendEmptyMessageDelayed(0, 1000);
                }
            } else if(msg.what == 1) {
                clearAnonymousData();
                AnonymousJoinMeetActivity.this.finish();
            } else if(msg.what == 2) {
                clearAnonymousData();
                AnonymousJoinMeetActivity.this.finish();
            }
        }
    };


}
