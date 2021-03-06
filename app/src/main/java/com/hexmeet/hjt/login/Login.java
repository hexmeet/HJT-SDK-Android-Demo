package com.hexmeet.hjt.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HexMeet;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.PermissionWrapper;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.AnonymousJoinMeetActivity;
import com.hexmeet.hjt.utils.PasswordDialog;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.event.LoginResultEvent;
import com.hexmeet.hjt.model.LoginParams;
import com.hexmeet.hjt.utils.ProgressUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
        if(BuildConfig.CLOUD_SERVER_ONLY){
            PreLoginFragment fragment = PreLoginFragment.newInstance(AppCons.LoginType.LOGIN_TYPE_CLOUD);
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment, PreLoginFragment.class.getName())
                    .addToBackStack(null).commitAllowingStateLoss();
        }else {
            MainLoginFragment fragment = MainLoginFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment, MainLoginFragment.class.getName())
                    .addToBackStack(null).commitAllowingStateLoss();
        }

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
            if (!HjtApp.getInstance().getAppService().isCalling()) {
                handleWebInvite(getIntent());
            }
        } else {
            handleWebInvite(getIntent());
        }
        turnPage();
    }

    private void turnPage() {
        LOG.info("cannotAutoLogin :" + LoginSettings.getInstance().cannotAutoLogin());
        if (LoginSettings.getInstance().cannotAutoLogin()) {
            progress.dismiss();
            gotoLoginPage();
            if(SystemCache.getInstance().isShowVersionDialog()){
                checkVersion(false);
            }
        } else {
                LOG.info("logined before this launch, start auto login");
                if (SystemCache.getInstance().isNetworkConnected()) {
                    progress.showDelayed(500);
                    LOG.info("network connected, start auto login");
                    new Thread() {@Override
                        public void run() { super.run();
                            try { Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace(); }
                            LoginService.getInstance().autoLogin();
                        }
                    }.start();
                }
        }
    }

    private void gotoLoginPage() {
        LOG.info("not login before this launch, jump to login_main UI");
        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, false);
        SystemCache.getInstance().resetLoginCache();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleWebInvite(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HjtApp.getInstance().getAppService() != null) {
            HjtApp.getInstance().getAppService().setUserInLogin(false);
           /* if (HjtApp.getInstance().getAppService().isCalling()) {
                LOG.info("onResume: Resume Call");
                Intent intent = new Intent();
                intent.setClass(Login.this, Conversation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }*/
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
        if(dialog != null && !dialog.isShowing()) {
            dialog.clean();
            dialog.dismiss();
        }
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
            LOG.info("gotoAnonymousMeeting()");
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
            LOG.info("Logon failure "+event.getCode());
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
            }else if(event.getCode() == LoginResultEvent.LOGIN_WRONG_NO_PERMISSION){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }else if(event.getCode() == LoginResultEvent.LOGIN_SDK_ERROR_8){
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            } else {
                Utils.showToastWithCustomLayout(Login.this, event.getMessage());
            }
           // HjtApp.getInstance().getAppService().logout();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        if (event.getCallState() == CallState.IDLE) {
            LOG.info("event : "+event.getCode()+",number : "+event.getNumber());
            if(event.getCode()==CallEvent.MRU_NORMAL || event.getCode()==CallEvent.MRU_OPERATOR_DISCONNECT){
                dialogTip(true,event.getNumber());
            }else if(event.getCode()==CallEvent.EP_NO_PACKET_RECEIVED || event.getCode()==CallEvent.MRU_NO_PACKET_RECEIVED){
                dialogTip(false,event.getNumber());
            }

        }
    }
    PasswordDialog dialog;
    private void dialogTip(boolean mru,String number) {
        dialog= new PasswordDialog.Builder(Login.this).setOkButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        }).setNetwork(mru)
                .setPasswordDialog(false)
                .setNumber(number)
                .createTwoButtonDialog();
        LOG.info("show dialog Tip");
        dialog.show();
    }
}
