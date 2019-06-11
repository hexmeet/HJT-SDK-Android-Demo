package com.hexmeet.hjt.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HexMeet;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.PermissionWrapper;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.AnonymousJoinMeetActivity;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.event.LoginResultEvent;
import com.hexmeet.hjt.model.LoginParams;
import com.hexmeet.hjt.utils.ProgressUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Login extends BaseActivity implements LoginFragmentCallback{
    private Logger LOG = Logger.getLogger(Login.class);
    private ProgressUtil progress = null;
    private LoginFragment loginFragment;


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, String error_msg) {
        Intent intent = new Intent(context, Login.class);
        intent.putExtra("error_msg", error_msg);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void actionStartAsInvite(Context context) {
        Intent intent = new Intent(context, Login.class);
        intent.putExtra(AppCons.INTENT_KEY_WEB_INVITE, true);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.debug("Login onCreate()");
        EventBus.getDefault().register(this);
        MainLoginFragment fragment = MainLoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, MainLoginFragment.class.getName())
                .addToBackStack(null).commitAllowingStateLoss();

        progress = new ProgressUtil(Login.this, 60000, new Runnable() {
            @Override
            public void run() {
                if(loginFragment != null) {
                    loginFragment.setLoginBtnEnable(true);
                }
                Utils.showToastWithCustomLayout(Login.this, getString(R.string.login_timeout));
            }
        }, getString(R.string.logging));

        String errorMessage = getIntent().getStringExtra("error_msg");
        if(!TextUtils.isEmpty(errorMessage)) {
            Utils.showToastWithCustomLayout(Login.this, errorMessage);
        }
        if (HjtApp.getInstance().getAppService() != null) {
            Log.i("isCalling", HjtApp.getInstance().getAppService().isCalling() + "");

            if (!HjtApp.getInstance().getAppService().isCalling()) {
                handleWebInvite(getIntent());
            }
        } else {

            handleWebInvite(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleWebInvite(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HjtApp.getInstance().getAppService().setUserInLogin(false);
        if(HjtApp.getInstance().getAppService().isCalling()) {
            LOG.info("onResume: Resume Call");
            Intent intent = new Intent();
            intent.setClass(Login.this, Conversation.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment mainFragment = getSupportFragmentManager().findFragmentByTag(MainLoginFragment.class.getName());
        if(mainFragment != null && mainFragment.isResumed()) {
            System.exit(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginFragment = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void gotoPrivateLogin() {
        PreLoginFragment fragment = PreLoginFragment.newInstance(AppCons.LoginType.LOGIN_TYPE_PRIVATE);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment, PreLoginFragment.class.getName())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void gotoCloudLogin() {
        PreLoginFragment fragment = PreLoginFragment.newInstance(AppCons.LoginType.LOGIN_TYPE_CLOUD);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment, PreLoginFragment.class.getName())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void onBackClick(String tag) {
        LOG.info("onBackClick->["+tag+"]");
        onBackPressed();
    }

    @Override
    public void gotoLoginDetail(int detailType) {
        loginFragment = LoginFragment.newInstance(detailType);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, loginFragment, LoginFragment.class.getName())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void doLogin(LoginParams locationParams, boolean https, String port) {
        LOG.info(" dologin ");
        progress.showDelayed(500);
        SystemCache.getInstance().setAnonymousMakeCall(false);
        HjtApp.getInstance().getAppService().loginInThread(locationParams, https, port);
    }


    @Override
    public void gotAdvanceSetting(boolean privateLogin) {
        AdvanceLoginFragment fragment = AdvanceLoginFragment.newInstance(privateLogin);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment, AdvanceLoginFragment.class.getName())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void dialOut() {
        progress.dismiss();
        if(PermissionWrapper.getInstance().checkMeetingPermission(Login.this)) {
            AnonymousJoinMeetActivity.gotoAnonymousMeeting(Login.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            AnonymousJoinMeetActivity.gotoAnonymousMeeting(Login.this);
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
            //can not make call as no permission
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginResultEvent event) {
        if(SystemCache.getInstance().isAnonymousMakeCall() || event.isAnonymous()) {
            return;
        }
        progress.dismiss();
        if(loginFragment != null) {
            loginFragment.setLoginBtnEnable(true);
        }

        if(event.getCode() == LoginResultEvent.LOGIN_SUCCESS) {
            Intent i = getIntent();
            if (i != null) {
                LOG.debug("login - onCreate  data:" + i.getData());
                HexMeet.actionStart(Login.this, i.getData());
            } else {
                LOG.debug("login - start hexmeet without data intent");
                HexMeet.actionStart(Login.this);
            }
            finish();
        }else {
            if(event.getCode() == LoginResultEvent.LOGIN_WRONG_PASSWORD || event.getCode() == LoginResultEvent.LOGIN_MANUAL_TRY){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }else if(event.getCode() == LoginResultEvent.LOGIN_WRONG_PASSWORD_TIME){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }else if(event.getCode() == LoginResultEvent.LOGIN_WRONG_INVALID_NAME){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }else if(event.getCode() == LoginResultEvent.LOGIN_WRONG_NET){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }else if(event.getCode() == LoginResultEvent.LOGIN_WRONG_LOCATION_SERVER){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            } else {
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }
            HjtApp.getInstance().getAppService().logout();
        }
    }

    private void handleWebInvite(Intent intent) {
        boolean isInvite = intent.getBooleanExtra(AppCons.INTENT_KEY_WEB_INVITE, false);
        if (!isInvite) {
            LOG.info("handleWebInvite Login is not Invited");
            return;
        }
        LOG.info("handleWebInvite Login Invited to dialOut");
        dialOut();
    }
}
