package com.hexmeet.hjt.login;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.model.LoginParams;

import org.apache.log4j.Logger;

public class LoginService {
    private Logger LOG = Logger.getLogger(LoginService.class);
    private static LoginService instance;

    public static LoginService getInstance() {
        if(instance == null) {
            instance = new LoginService();
        }
        return instance;
    }

    private LoginService() {}

    public void anonymousMakeCall() {
        HjtApp.getInstance().getAppService().anonymousMakeCall();
    }

    public void autoLogin() {
        LOG.info("isAnonymousMakeCall : "+SystemCache.getInstance().isAnonymousMakeCall());
        if(!SystemCache.getInstance().isAnonymousMakeCall()){
            boolean isCloudLogin = LoginSettings.getInstance().getLoginState(false) == LoginSettings.LOGIN_CLOUD_SUCCESS;
            LOG.info("iscloud : "+isCloudLogin+"");
            LoginParams params = new LoginParams();
            params.setPassword(LoginSettings.getInstance().getPassword(isCloudLogin));
            params.setServerAddress(isCloudLogin ? LoginSettings.LOCATION_CLOUD : LoginSettings.getInstance().getPrivateLoginServer());
            params.setUser_name(LoginSettings.getInstance().getUserName(isCloudLogin));
            boolean https = isCloudLogin || LoginSettings.getInstance().useHttps();
            String port = isCloudLogin ? null : LoginSettings.getInstance().getPrivatePort();
            SystemCache.getInstance().setCloudLogin(isCloudLogin);
            LOG.info("autoLogin()  https : "+https+",port : "+port);
            HjtApp.getInstance().getAppService().loginInThread(params, https, port);
        }
    }
}
