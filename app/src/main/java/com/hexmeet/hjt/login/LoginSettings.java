package com.hexmeet.hjt.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.HjtApp;

public class LoginSettings {
    public static final String LOCATION_CLOUD = BuildConfig.CLOUD_LOCATION_SERVER_ADDR;
    public static final String LOGIN_CLOUD_SERVER = BuildConfig.CLOUD_SERVER_ADDR;
    public static final int LOGIN_STATE_IDLE = 0;
    public static final int LOGIN_CLOUD_SUCCESS = 1;
    public static final int LOGIN_PRIVATE_SUCCESS = 2;

    private static final String LOGIN_SETTINGS = "login_settings";
    private static final String DEFAULT_STR = "";
    private static LoginSettings instance;
    private SharedPreferences sp;

    private String privateLoginServer;
    private String privateUserName;
    private String privatePassword;
    private String privatePort;
    private boolean useHttps;

    private String cloudUserName;
    private String cloudPassword;


    private String privateJoinMeetingServer;
    private String privateJoinMeetingDisplayName;
    private String privateJoinMeetingNumberId;
    private String privateJoinMeetingPort;
    private boolean useJoinMeetingHttps;

    private String cloudNumberId;
    private String cloudDisplayName;

    private boolean isCloudMuteVideo;
    private boolean isCloudMuteMic;

    private boolean isPrivateMuteVideo;
    private boolean isPrivateMuteMic;


    private String testServer;

    private int loginState, anonymousLoginState;
    private String inviteUserName;
    private LoginSettings() {
        init(HjtApp.getInstance().getContext());
    }

    interface Key {
        String LOGIN_SERVER_PRIVATE = "private_login_server";
        String LOGIN_USER_NAME_PRIVATE = "private_username";
        String LOGIN_PASSWORD_PRIVATE = "private_password";
        String LOGIN_PORT_PRIVATE = "private_login_port";
        String LOGIN_HTTPS_PRIVATE = "private_use_https";

        String LOGIN_USER_NAME_CLOUD = "cloud_username";
        String LOGIN_PASSWORD_CLOUD = "cloud_password";

        String LOGIN_TEST_SERVER = "test_server_address";

        String LOGIN_STATE = "login_state";
        String INVITE_USERNAME = "invite_username";

        String LOGIN_NUMBER_ID_CLOUD = "cloud_number_id";
        String LOGIN_DISPLAY_CLOUD = "cloud_display";

         String PRIVATE_JOINMEETING_SERVER ="private_joinMeeting_server";
         String PRIVATE_JOINMEETING_DISPLAYNAME = "private_joinMeeting_displayname";
         String PRIVATE_JOINMEETING_NUMBER = "private_joinMeeting_number";
         String PRIVATE_JOINMEETING_PORT = "private_joinMeeting_port";
        String USE_JOINMEETING_HTTPS = "use_joinMeeting_https";

        String is_CloudMuteVideo = "is_MuteVideo";
        String is_CloudMuteMic= "is_MuteMic";

        String is_PrivatMuteVideo = "is_PrivatMuteVideo";
        String is_PrivatMuteMic= "is_PrivatMuteMic";
    }

    public static LoginSettings getInstance() {
        if (instance == null) {
            instance = new LoginSettings();
        }
        return instance;
    }

    private void init(Context context) {
        sp = context.getSharedPreferences(LOGIN_SETTINGS, Context.MODE_PRIVATE);

        privateLoginServer = sp.getString(Key.LOGIN_SERVER_PRIVATE, DEFAULT_STR);
        privateUserName = sp.getString(Key.LOGIN_USER_NAME_PRIVATE, DEFAULT_STR);
        privatePassword = sp.getString(Key.LOGIN_PASSWORD_PRIVATE, DEFAULT_STR);
        privatePort = sp.getString(Key.LOGIN_PORT_PRIVATE, DEFAULT_STR);
        useHttps = sp.getBoolean(Key.LOGIN_HTTPS_PRIVATE, false);

        cloudUserName = sp.getString(Key.LOGIN_USER_NAME_CLOUD, DEFAULT_STR);
        cloudPassword = sp.getString(Key.LOGIN_PASSWORD_CLOUD, DEFAULT_STR);

        testServer = sp.getString(Key.LOGIN_TEST_SERVER, DEFAULT_STR);

        loginState = sp.getInt(Key.LOGIN_STATE, LOGIN_STATE_IDLE);

        inviteUserName = sp.getString(Key.INVITE_USERNAME, DEFAULT_STR);
        anonymousLoginState = LOGIN_STATE_IDLE;

        cloudNumberId = sp.getString(Key.LOGIN_NUMBER_ID_CLOUD,DEFAULT_STR);
        cloudDisplayName = sp.getString(Key.LOGIN_DISPLAY_CLOUD,DEFAULT_STR);

        privateJoinMeetingServer = sp.getString(Key.PRIVATE_JOINMEETING_SERVER,DEFAULT_STR);
        privateJoinMeetingDisplayName= sp.getString(Key.PRIVATE_JOINMEETING_DISPLAYNAME,DEFAULT_STR);
        privateJoinMeetingNumberId= sp.getString(Key.PRIVATE_JOINMEETING_NUMBER,DEFAULT_STR);
        privateJoinMeetingPort= sp.getString(Key.PRIVATE_JOINMEETING_PORT,DEFAULT_STR);
        useJoinMeetingHttps= sp.getBoolean(Key.USE_JOINMEETING_HTTPS,false);

        isCloudMuteVideo= sp.getBoolean(Key.is_CloudMuteVideo,false);
        isCloudMuteMic= sp.getBoolean(Key.is_CloudMuteMic,false);

        isPrivateMuteVideo= sp.getBoolean(Key.is_PrivatMuteVideo,false);
        isPrivateMuteMic= sp.getBoolean(Key.is_PrivatMuteMic,false);
    }

    public String getPrivateLoginServer() {
        return privateLoginServer;
    }

    public void setPrivateLoginServer(String loginServer) {
        if (!TextUtils.equals(this.privateLoginServer, loginServer)) {
            this.privateLoginServer = loginServer;
            sp.edit().putString(Key.LOGIN_SERVER_PRIVATE, loginServer).apply();
        }
    }

    public String getUserName(boolean isCloud) {
        return isCloud ? cloudUserName : privateUserName;
    }

    public void setCloudUserName(String userName) {
        if (!TextUtils.equals(this.cloudUserName, userName)) {
            this.cloudUserName = userName;
            sp.edit().putString(Key.LOGIN_USER_NAME_CLOUD, userName).apply();
        }

    }

    public void setPrivateUserName(String userName) {
        if (!TextUtils.equals(this.privateUserName, userName)) {
            this.privateUserName = userName;
            sp.edit().putString(Key.LOGIN_USER_NAME_PRIVATE, userName).apply();
        }

    }

    public String getPassword(boolean isCloud) {
        Log.i("password===",isCloud+"==="+cloudPassword +"==="+privatePassword);
        return isCloud ? cloudPassword : privatePassword;
    }

    public void setPrivatePassword(String password) {
        if (!TextUtils.equals(this.privatePassword, password)) {
            this.privatePassword = password;
            sp.edit().putString(Key.LOGIN_PASSWORD_PRIVATE, password).apply();
        }
    }

    public void setCloudPassword(String password) {
        Log.i("setCloudPassword", password);
        if (!TextUtils.equals(this.cloudPassword, password)) {
            this.cloudPassword = password;
            sp.edit().putString(Key.LOGIN_PASSWORD_CLOUD, password).apply();
        }
    }

    public String getPrivatePort() {
        return privatePort;
    }

    public void setPrivatePort(String port) {
        if (!TextUtils.equals(this.privatePort, port)) {
            this.privatePort = port;
            sp.edit().putString(Key.LOGIN_PORT_PRIVATE, port).apply();
        }
    }

    public String getTestServer() {
        return testServer;
    }

    public void setTestServer(String testServer) {
        if(testServer == null) {
            this.testServer = null;
            sp.edit().remove(Key.LOGIN_TEST_SERVER).apply();
            return;
        }
        if (!TextUtils.equals(this.testServer, testServer)) {
            this.testServer = testServer;
            sp.edit().putString(Key.LOGIN_TEST_SERVER, testServer).apply();
        }
    }

    public boolean useHttps() {
        return loginState != LOGIN_CLOUD_SUCCESS && useHttps;
    }

    public void setUseHttps(boolean useHttps) {
        if (this.useHttps ^ useHttps) {
            this.useHttps = useHttps;
            sp.edit().putBoolean(Key.LOGIN_HTTPS_PRIVATE, useHttps).apply();
        }
    }

    public int getLoginState(boolean isAnonymous) {
        return isAnonymous ? anonymousLoginState :loginState;
    }
    public void setLoginState(int state, boolean isAnonymous) {
        if(isAnonymous) {
            anonymousLoginState = state;
        } else {
            if(loginState != state) {
                loginState = state;
                sp.edit().putInt(Key.LOGIN_STATE, state).apply();
            }
        }
    }

    public boolean isCloudLoginSuccess() {
        return loginState == LOGIN_CLOUD_SUCCESS;
    }

    public boolean cannotAutoLogin() {
        if(loginState == LoginSettings.LOGIN_STATE_IDLE) {
            return true;
        }

        if(loginState == LoginSettings.LOGIN_CLOUD_SUCCESS) {
            return TextUtils.isEmpty(getUserName(true))
                    || TextUtils.isEmpty(getPassword(true));
        }

        if(loginState == LoginSettings.LOGIN_PRIVATE_SUCCESS) {
            return TextUtils.isEmpty(getUserName(false))
                    || TextUtils.isEmpty(getPassword(false))
                    || TextUtils.isEmpty(privateLoginServer);
        }
        return false;
    }

    public String getInviteUserName() {
        return inviteUserName;
    }

    public void setInviteUserName(String userName) {
        inviteUserName = userName;
        sp.edit().putString(Key.INVITE_USERNAME, userName).apply();
    }


    public String getPrivateJoinMeetingServer() {
        return privateJoinMeetingServer;
    }

    public void setPrivateJoinMeetingServer(String joinMeetingServer) {
        if (!TextUtils.equals(this.privateJoinMeetingServer, joinMeetingServer)) {
            this.privateJoinMeetingServer = joinMeetingServer;
            sp.edit().putString(Key.PRIVATE_JOINMEETING_SERVER, joinMeetingServer).apply();
        }
    }

    public String getCloudNumberId() {
        return cloudNumberId;
    }

    public void setCloudNumberId(String number) {
        if (!TextUtils.equals(this.cloudNumberId, number)) {
            this.cloudNumberId = number;
            sp.edit().putString(Key.LOGIN_NUMBER_ID_CLOUD, number).apply();
        }

    }

    public String getPrivateJoinMeetingNumberId() {
        return privateJoinMeetingNumberId;
    }

    public void setPrivateJoinMeetingNumberId(String joinMeetingNumber) {
        if (!TextUtils.equals(this.privateJoinMeetingNumberId, joinMeetingNumber)) {
            this.privateJoinMeetingNumberId = joinMeetingNumber;
            sp.edit().putString(Key.PRIVATE_JOINMEETING_NUMBER, joinMeetingNumber).apply();
        }
    }

    public String getCloudDisplayName() {
        return cloudDisplayName;
    }
    public void setCloudDisplayName(String displayName) {
        if (!TextUtils.equals(this.cloudDisplayName, displayName)) {
            this.cloudDisplayName = displayName;
            sp.edit().putString(Key.LOGIN_DISPLAY_CLOUD, displayName).apply();
        }

    }

    public String getPrivateJoinMeetingDisplayName() {
        return privateJoinMeetingDisplayName;
    }

    public void setPrivateJoinMeetingDisplayName(String joinMeetingDisplayName) {
        if (!TextUtils.equals(this.privateJoinMeetingDisplayName, joinMeetingDisplayName)) {
            this.privateJoinMeetingDisplayName = joinMeetingDisplayName;
            sp.edit().putString(Key.PRIVATE_JOINMEETING_DISPLAYNAME, joinMeetingDisplayName).apply();
        }
    }


    public String getPrivateJoinMeetingPort() {
        return privateJoinMeetingPort;
    }

    public void setPrivateJoinMeetingPort(String joinMeetingPort) {
        if (!TextUtils.equals(this.privateJoinMeetingPort, joinMeetingPort)) {
            this.privateJoinMeetingPort = joinMeetingPort;
            sp.edit().putString(Key.PRIVATE_JOINMEETING_PORT, joinMeetingPort).apply();
        }
    }

    public boolean getJoinMeetingHttps() {
        return useJoinMeetingHttps;
    }

    public void setJoinMeetingHttps(boolean joinMeetingHttps) {
        if (this.useJoinMeetingHttps ^ joinMeetingHttps) {
            this.useJoinMeetingHttps = joinMeetingHttps;
            sp.edit().putBoolean(Key.USE_JOINMEETING_HTTPS, joinMeetingHttps).apply();
        }
    }

    public boolean isMuteVideo(boolean isCloud) {
        return isCloud ? isCloudMuteVideo : isPrivateMuteVideo;
    }

    public void setCloudMuteVideo(boolean muteVideo) {
        if (this.isCloudMuteVideo ^ muteVideo) {
            this.isCloudMuteVideo = muteVideo;
            sp.edit().putBoolean(Key.is_CloudMuteVideo, muteVideo).apply();
        }
    }

    public void setPrivateMuteVideo(boolean muteVideo) {
        if (this.isPrivateMuteVideo ^ muteVideo) {
            this.isPrivateMuteVideo = muteVideo;
            sp.edit().putBoolean(Key.is_PrivatMuteVideo, muteVideo).apply();
        }
    }

    public boolean isMuteMic(boolean isCloud) {
        return isCloud ? isCloudMuteMic : isPrivateMuteMic;
    }

    public void setCloudMuteMic(boolean muteMic) {
        if (this.isCloudMuteMic ^ muteMic) {
            this.isCloudMuteMic = muteMic;
            sp.edit().putBoolean(Key.is_CloudMuteMic, muteMic).apply();
        }
    }

    public void setPrivateMuteMic(boolean muteMic) {
        if (this.isPrivateMuteMic ^ muteMic) {
            this.isPrivateMuteMic = muteMic;
            sp.edit().putBoolean(Key.is_PrivatMuteMic, muteMic).apply();
        }
    }
}
