package com.hexmeet.hjt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import cn.jpush.android.api.JPushInterface;

public class JPushReceiver extends BroadcastReceiver {

    private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(this.getClass());


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
                //极光服务器分配的Registration Id，
                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
                LOG.info("[MyReceiver] 接收Registration Id : " + regId);
            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
                //自定义消息
                LOG.info("收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
                // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                //推送通知
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                LOG.info("[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
                LOG.info("点击通知 ： "+bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE)+"，内容："+bundle.getString(JPushInterface.EXTRA_ALERT));
                //当用户点击通知时的操作,打开自定义的Activity
               Intent i = new Intent(context, HexMeet.class);
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        } catch (Exception e) {

        }
    }


}


