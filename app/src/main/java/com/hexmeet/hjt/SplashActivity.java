package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.alibaba.sdk.android.push.AndroidPopupActivity;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.utils.AlertDialogUtil;
import com.hexmeet.hjt.utils.ResourceUtils;

import java.util.Map;

import androidx.annotation.NonNull;

public class SplashActivity extends BaseActivity {
    private final int TIMEOUT = -100;
    private final int WAIT_SERVICE = 100;
    private AlertDialogUtil dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("App splash onCreate");
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        if ((HjtApp.isCnVersion() && BuildConfig.SHOW_PRIVACY_WINDOW) && (!LoginSettings.getInstance().getPrivacyPolicy() && !SystemCache.getInstance().isInviteMakeCall())){
            dialog = new AlertDialogUtil.Builder(SplashActivity.this)
                    .setPositiveButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//同意
                            LoginSettings.getInstance().setPrivacyPolicy(true);
                            dialog.dismiss();
                            autoLoginHandler.sendEmptyMessage(WAIT_SERVICE);
                        }
                    }).setNegativeButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//取消
                            dialog.dismiss();
                            finish();
                        }
                    }).createTwoButtonDialog();
            dialog.show();
        }else {
            autoLoginHandler.sendEmptyMessage(WAIT_SERVICE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LOG.info("App splash onStart");
    }

    @SuppressLint("HandlerLeak")
    private Handler autoLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIMEOUT) {
                LOG.info("splash timeout after 1 seconds");
                Login.actionStart(SplashActivity.this);
                finish();
            }

            if (msg.what == WAIT_SERVICE) {
                if(HjtApp.getInstance().getAppService() != null && SystemCache.getInstance().isSdkReady()) {
                    if(PermissionWrapper.getInstance().checkStoragePermission(SplashActivity.this)) {
                        HjtApp.getInstance().initLogs();
                        turnPage();
                    }
                } else {
                    LOG.info("Sdk|AppService not ready");
                    sendEmptyMessageDelayed(WAIT_SERVICE, 1000);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            turnPage();
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
             finish();
        }
    }

    private void turnPage() {
        ResourceUtils.getInstance().initScreenSize();
        autoLoginHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Login.actionStart(SplashActivity.this);
                finish();
            }
        },1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOG.info(" onDestroy()");
    }


    class PopupPushActivity  extends AndroidPopupActivity {

        @Override
        protected void onSysNoticeOpened(String title, String content, Map<String, String> extraMap) {
            LOG.info("Receive ThirdPush notification, title: " + title + ", content: " + content + ", extraMap: " + extraMap);
            Intent intent = new Intent(this,SplashActivity.class);
            startActivity(intent);
        }
    }

}