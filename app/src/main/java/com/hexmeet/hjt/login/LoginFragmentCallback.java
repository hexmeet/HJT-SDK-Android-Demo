package com.hexmeet.hjt.login;

import com.hexmeet.hjt.model.LoginParams;

public interface LoginFragmentCallback {
    void gotoPrivateLogin();
    void gotoCloudLogin();
    void onBackClick(String tag);
    void gotoLoginDetail(int detailType);
    void gotAdvanceSetting(boolean privateLogin);
    void dialOut();
    void doLogin(LoginParams params, boolean https, String port);
}
