package com.hexmeet.hjt.push;


import android.content.Context;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;

import org.apache.log4j.Logger;

import java.util.Map;

public class MyMessageReceiver extends MessageReceiver {
    // 消息接收部分的LOG_TAG
    private Logger LOG = Logger.getLogger(MyMessageReceiver.class);
    // 推送通知的回调方法
    @Override
    public void onNotification(Context context, String title, String summary, Map<String, String> extraMap) {
        // TODO 处理推送通知
        LOG.info("MyMessageReceiver  :  Receive notification, title: " + title + ", summary: " + summary + ", extraMap: " + extraMap);
      //HjtApp.getInstance().createNotificationChannel(context,title,summary);

    }

    //推送消息的回调方法
    @Override
    public void onMessage(Context context, CPushMessage cPushMessage) {
        LOG.info("MyMessageReceiver : onMessage, messageId: " + cPushMessage.getMessageId() + ", title: " + cPushMessage.getTitle() + ", content:" + cPushMessage.getContent());
       // HjtApp.getInstance().createNotificationChannel(context,cPushMessage.getTitle(),cPushMessage.getContent());
    }

    //从通知栏打开通知的扩展处理
    @Override
    public void onNotificationOpened(Context context, String title, String summary, String extraMap) {
        LOG.info("MyMessageReceiver : onNotificationOpened, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
    }

    //无动作通知点击回调。当在后台或阿里云控制台指定的通知动作为无逻辑跳转时,通知点击回调为onNotificationClickedWithNoAction而不是onNotificationOpened
    @Override
    protected void onNotificationClickedWithNoAction(Context context, String title, String summary, String extraMap) {
        LOG.info("MyMessageReceiver : onNotificationClickedWithNoAction, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
    }

    //应用处于前台时通知到达回调
    @Override
    protected void onNotificationReceivedInApp(Context context, String title, String summary, Map<String, String> extraMap, int openType, String openActivity, String openUrl) {
        LOG.info("MyMessageReceiver : onNotificationReceivedInApp, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap + ", openType:" + openType + ", openActivity:" + openActivity + ", openUrl:" + openUrl);
    }

    // 通知删除回调
    @Override
    protected void onNotificationRemoved(Context context, String messageId) {
        LOG.info("MyMessageReceiver : onNotificationRemoved");
    }


}
