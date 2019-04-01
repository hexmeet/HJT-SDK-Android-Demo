package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.LoginResultEvent;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.login.LoginService;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.utils.ResourceUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SplashActivity extends BaseActivity {
    private final int TIMEOUT = -100;
    private final int WAIT_SERVICE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("App splash onCreate");
        EventBus.getDefault().register(this);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        autoLoginHandler.sendEmptyMessage(WAIT_SERVICE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginResultEvent event) {
        if (event.getCode() == LoginResultEvent.LOGIN_SUCCESS) {
            LOG.info("Auto Login success, go to Hexmeet");
            HexMeet.actionStart(SplashActivity.this);
        } else if (event.getCode() == LoginResultEvent.LOGIN_WRONG_PASSWORD || event.getCode() == LoginResultEvent.LOGIN_MANUAL_TRY) {
            Login.actionStart(SplashActivity.this, event.getMessage());
        }else {
            LOG.info("Auto Login failed, go to Login");
            Login.actionStart(SplashActivity.this, event.getMessage());
        }
        autoLoginHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @SuppressLint("HandlerLeak")
    private Handler autoLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIMEOUT) {
                LOG.info("splash timeout after 5 seconds");
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
        if (LoginSettings.getInstance().cannotAutoLogin()) {
            gotoLoginPage();
        } else {
            LOG.info("logined before this launch, start auto location");
            if (SystemCache.getInstance().isNetworkConnected()) {
                LOG.info("network connected, start auto location");
                LoginService.getInstance().autoLogin();
            }
            autoLoginHandler.sendEmptyMessageDelayed(TIMEOUT, 8000);
        }
    }

    private void gotoLoginPage() {
        LOG.info("not login before this launch, jump to login_main UI");
        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, false);
        Login.actionStart(SplashActivity.this);
        SystemCache.getInstance().resetLoginCache();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}