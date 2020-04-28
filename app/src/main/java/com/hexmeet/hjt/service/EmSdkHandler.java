package com.hexmeet.hjt.service;

import android.os.HandlerThread;
import android.os.Message;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.model.IMLoginParams;
import com.hexmeet.hjt.sdk.EmSdkManager;

import org.apache.log4j.Logger;

public class EmSdkHandler  extends BaseSafelyHandler<EmSdkManager> {
    private Logger LOG = Logger.getLogger(EmSdkHandler.class);

    final static int HANDLER_EMSDK_INIT = 10001;
    final static int HANDLER_EMSDK_SENDMESSAGE = 10002;
    final static int HANDLER_EMSDK_GROUPMEMBERNAME = 10003;
    final static int HANDLER_EMSDK_ANONYMOUSLOGIN = 10004;
    final static int HANDLER_EMSDK_JOINGROUPCHAT = 10005;
    final static int HANDLER_EMSDK_RELEASEEMSDK = 10006;
    final static int HANDLER_EMSDK_LOGAUT = 10007;
    final static int HANDLER_SDK_ENABLE_SECURE = 10008;
    EmSdkHandler(HandlerThread thread, EmSdkManager ref) {
        super(thread.getLooper(), ref);
    }

    @Override
    public void handleMessage(EmSdkManager ref, Message msg) {
        LOG.info("Handle SDK message: (msg.what) = " + msg.what);
        switch (msg.what) {
            case HANDLER_EMSDK_INIT:
                ref.initEmSDK();
                break;
            case HANDLER_EMSDK_SENDMESSAGE:
                String groupId = msg.getData().getString("groupId");
                String content = msg.getData().getString("content");
                ref.sendMessage(groupId,content);
                break;
            case  HANDLER_EMSDK_GROUPMEMBERNAME :

                break;
            case  HANDLER_EMSDK_ANONYMOUSLOGIN :
                IMLoginParams params = msg.getData().getParcelable(AppCons.BundleKeys.EXTRA_DATA);
                if(params != null) {
                    ref.anonymousLogin(params);
                }
                break;
            case  HANDLER_EMSDK_JOINGROUPCHAT:
                String group = (String) msg.obj;
                ref.joinGroup(group);
                break;
            case HANDLER_EMSDK_RELEASEEMSDK:
                ref.releaseEmSdk();
                break;
            case HANDLER_EMSDK_LOGAUT:
                ref.logout();
                break;
            case HANDLER_SDK_ENABLE_SECURE:
                ref.enableSecure(msg.arg1 == 1);
                break;
            default:
                break;
        }
    }
}
