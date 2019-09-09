package com.hexmeet.hjt;

import android.content.Context;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 自定义JPush message 接收器,包括操作tag/alias的结果返回(仅仅包含tag/alias新接口部分)
 * */
public class MyJPushMessageReceiver extends JPushMessageReceiver {
    private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(this.getClass());

    //tag 增删查改的操作会在此方法中回调结果。
    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        LOG.info("onTagOperatorResult : "+jPushMessage.toString());
        TagAliasOperatorHelper.getInstance().onTagOperatorResult(context,jPushMessage);
        super.onTagOperatorResult(context, jPushMessage);
    }


    //查询某个 tag 与当前用户的绑定状态的操作会在此方法中回调结果。
    @Override
    public void onCheckTagOperatorResult(Context context,JPushMessage jPushMessage){
        LOG.info("onCheckTagOperatorResult : "+jPushMessage.toString());
        TagAliasOperatorHelper.getInstance().onCheckTagOperatorResult(context,jPushMessage);
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

     //alias 相关的操作会在此方法中回调结果。
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        LOG.info("onAliasOperatorResult : "+jPushMessage.toString());
        TagAliasOperatorHelper.getInstance().onAliasOperatorResult(context,jPushMessage);
        super.onAliasOperatorResult(context, jPushMessage);
    }
    //设置手机号码会在此方法中回调结果。
    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        LOG.info("onMobileNumberOperatorResult : "+jPushMessage.toString());
        TagAliasOperatorHelper.getInstance().onMobileNumberOperatorResult(context,jPushMessage);
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }
    //收到自定义消息
    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        super.onMessage(context, customMessage);
        LOG.info("message : "+customMessage.toString());
    }

}
