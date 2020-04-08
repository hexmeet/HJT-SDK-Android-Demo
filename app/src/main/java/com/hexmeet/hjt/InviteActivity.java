package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import pl.droidsonroids.gif.GifImageView;

public class InviteActivity extends BaseActivity{
    private Logger LOG = Logger.getLogger(InviteActivity.class);
    private final int WAIT_SERVICE = 10;

    private FormEditText displayNameEdit;
    private Button okBtn,no_btn;
    private TextView invite_number;
    private Uri uri;
    private LinearLayout invite_join;
    private CheckBox invite_camera1,invite_mic;
    private GifImageView invite_gif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("InviteActivity onCreate");
        setContentView(R.layout.web_invite);

        SystemCache.getInstance().setInviteMakeCall(true);
        invite_gif = (GifImageView) findViewById(R.id.invite_gif);
        displayNameEdit = findViewById(R.id.display_name);
        invite_number = (TextView)findViewById(R.id.invite_number);
        invite_join = (LinearLayout)findViewById(R.id.invite_join);

        no_btn = findViewById(R.id.no_btn);
        no_btn.setOnClickListener(clickListener);
        okBtn = findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(clickListener);

        invite_camera1 = (CheckBox)findViewById(R.id.invite_camera);
        invite_mic = (CheckBox)findViewById(R.id.invite_mic);
        invite_mic.setChecked(SystemCache.getInstance().isUserMuteMic());
        invite_mic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SystemCache.getInstance().setUserMuteMic(isChecked);
            }
        });
        displayNameEdit.setText(LoginSettings.getInstance().getInviteUserName());
        boolean isNetworkOk = NetworkUtil.isNetConnected(this);
        SystemCache.getInstance().setNetworkConnected(isNetworkOk);
        if (isNetworkOk) {
            handler.sendEmptyMessage(WAIT_SERVICE);
        } else {
            Utils.showToast(this, R.string.network_unconnected);
            startAppFromSplash();
        }
        inviteUri();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.no_btn:
                    startAppFromSplash();
                    break;
                case R.id.ok_btn:
                    String displayName = displayNameEdit.getText().toString().trim();
                    if(TextUtils.isEmpty(displayName)) {
                        displayName = Build.MODEL;
                    }else if (TextUtils.getTrimmedLength(displayName)>16){
                        Utils.showToast(InviteActivity.this, R.string.displayname_max_length);
                        return;
                    }else {
                        LoginSettings.getInstance().setInviteUserName(displayName);
                    }
                    SystemCache.getInstance().getJoinMeetingParam().setDisplayName(displayName);
                    turnPageToAnonymousDialOut();
                    break;

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ResourceUtils.getInstance().initScreenSize();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private boolean isSdkOk() {
        return HjtApp.getInstance().getAppService() != null && SystemCache.getInstance().isSdkReady();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WAIT_SERVICE) {
                if(isSdkOk()) {
                    if(PermissionWrapper.getInstance().checkStoragePermission(InviteActivity.this)) {
                        PermissionWrapper.getInstance().hasFloatWindowPermission(InviteActivity.this, true);
                        SystemCache.getInstance().setInviteMakeCall(true);
                        HjtApp.getInstance().initLogs();
                        if(HjtApp.getInstance().getAppService().isCalling()) {
                            LOG.info("onResume: Resume Call");
                            HjtApp.getInstance().stopFloatService();
                            Intent intent = new Intent();
                            intent.setClass(InviteActivity.this, Conversation.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }else {
                            handleInviteUri();
                        }
                    }
                } else {
                    LOG.info("Sdk|AppService not ready");
                    sendEmptyMessageDelayed(WAIT_SERVICE, 1000);
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            PermissionWrapper.getInstance().hasFloatWindowPermission(InviteActivity.this, true);
            SystemCache.getInstance().setInviteMakeCall(true);
            if(HjtApp.getInstance().getAppService().isCalling()) {
                LOG.info("onResume: Resume Call");
                HjtApp.getInstance().stopFloatService();
                Intent intent = new Intent();
                intent.setClass(InviteActivity.this, Conversation.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }else {
                handleInviteUri();
            }
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
            finish();
        }
    }
    private void inviteUri() {
        uri = getIntent().getData();
        if(uri == null) {
            startAppFromSplash();
            return;
        }
        if(TextUtils.isEmpty(uri.getQueryParameter("confid"))){
            startAppFromSplash();
            return;
        }
        invite_number.setText(uri.getQueryParameter("confid"));
    }
    private void handleInviteUri() {

        LOG.info("-->handleWebInvite uri: " + uri);
        String path = uri.getLastPathSegment();
        LOG.info("handleWebInvite path:[" + path + "]");
        if (!TextUtils.equals(path, "join") && !TextUtils.equals(path, "location")) {
            startAppFromSplash();
            return;
        }

        boolean isLocationUri = TextUtils.equals(path, "location");

        String number = uri.getQueryParameter("confid");
        String protocol = uri.getQueryParameter("protocol");
        String port = uri.getQueryParameter("port");
        String displayName = uri.getQueryParameter("displayname");
        String password = uri.getQueryParameter("password");
        String host = uri.getHost();

        LOG.info("handleWebInvite params: {number:["+number+"];protocol:["+protocol+"];host:["+host+"];port:["+port+"];displayName:["+displayName+"];password:["+password+"]}");
        if(TextUtils.isEmpty(protocol) || TextUtils.isEmpty(host) || TextUtils.isEmpty(number)) {
            startAppFromSplash();
            return;
        }

        boolean isHttps = TextUtils.equals(protocol, "https");

        SystemCache.getInstance().getJoinMeetingParam().setServer(host);
        SystemCache.getInstance().getJoinMeetingParam().setConferenceNumber(number);
        SystemCache.getInstance().getJoinMeetingParam().setPassword(password);
        SystemCache.getInstance().getJoinMeetingParam().setDisplayName(displayName);
        SystemCache.getInstance().getJoinMeetingParam().setPort(port);
        SystemCache.getInstance().getJoinMeetingParam().setUseHttps(isHttps);
        SystemCache.getInstance().getJoinMeetingParam().setCloud(host.equalsIgnoreCase(LoginSettings.LOCATION_CLOUD) || host.equalsIgnoreCase(LoginSettings.LOGIN_CLOUD_SERVER));

        handJoinMeeting(host, isLocationUri);
    }

    private void handJoinMeeting(String targetServer, boolean isLocation) {
        boolean isSvcReg = SystemCache.getInstance().getRegisterState() == RegisterState.SUCCESS;
        boolean dialOutDirectly = false;
        LOG.info("isLocation : "+isLocation+",isSvcReg : "+isSvcReg);
        if(isLocation && isSvcReg) {
            boolean isCloudLogin = SystemCache.getInstance().isCloudLogin();
            String locationServer = isCloudLogin ? LoginSettings.LOCATION_CLOUD : LoginSettings.getInstance().getPrivateLoginServer();
            LOG.info("server : "+locationServer+",targetServer : "+targetServer);
            dialOutDirectly = TextUtils.equals(locationServer, targetServer);
        }

        if(dialOutDirectly) {
            gotoHexmeet(true);
            return;
        }

        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, true);
        if (!TextUtils.isEmpty(SystemCache.getInstance().getJoinMeetingParam().getDisplayName())) {
            turnPageToAnonymousDialOut();
        } else {
            invite_gif.setVisibility(View.GONE);
            invite_join.setVisibility(View.VISIBLE);
        }

    }

    private void gotoHexmeet(boolean directDialOut) {
        LOG.info("handleWebInvite goto Hexmeet and do AnonymousDialOut, directDialOut : "+directDialOut);
        HexMeet.actionStartAsInvite(InviteActivity.this, directDialOut ? AppCons.INTENT_VALUE_WEB_INVITE_DIAOUT : AppCons.INTENT_VALUE_WEB_INVITE_ANONYMOUS);
        finish();
    }

    private void gotoLogin() {
        LOG.info("handleWebInvite goto Login and do AnonymousDialOut");
        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, false);
        Login.actionStartAsInvite(InviteActivity.this);
        SystemCache.getInstance().resetLoginCache();
        finish();
    }

    private void turnPageToAnonymousDialOut() {
        if (SystemCache.getInstance().getLoginResponse() != null) {
            gotoHexmeet(false);
            // TODO - it's not correct to go HexMeet page first for an anonymous call scenario.
            // Should start anonymous call first, then after the call we determine whether to
            // do a auto login
            //gotoLogin();
        } else {
            gotoLogin();
        }
        SystemCache.getInstance().setCamera(!invite_camera1.isChecked());
        HjtApp.getInstance().getAppService().setVideoMode(true);
        HjtApp.getInstance().getAppService().enableVideo(!invite_camera1.isChecked());
        HjtApp.getInstance().getAppService().muteMic(invite_mic.isChecked());
    }



    private void startAppFromSplash() {
        SystemCache.getInstance().setJoinMeetingParam(null);
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}
