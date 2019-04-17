package com.hexmeet.hjt.conf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.utils.Utils;
import com.hexmeet.hjt.wxapi.WXEntryActivity;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WeChat {

    public static void share(Activity context, MeetingForWechat meeting) {
        share(context, meeting, false);
    }

    @SuppressLint("SimpleDateFormat")
    public static void share(Activity context, MeetingForWechat meeting, boolean fromSchedulingOK) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, WXEntryActivity.APP_ID, true);
        api.registerApp(WXEntryActivity.APP_ID);

        if (!api.isWXAppInstalled()) {
            Utils.showToast(context, context.getResources().getString(R.string.client_not_installed));
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = meeting.getUrl();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        StringBuilder sb = new StringBuilder();
        msg.title = meeting.getMeetingName();
        if(!TextUtils.isEmpty(meeting.getConfTime())) {
            sb.append(meeting.getConfTime());
        }
        sb.append("\nID:" + meeting.getNumericId()
                + (isSomething(meeting.getPassword()) ? "*" + meeting.getMeetingPassword() : ""));
        msg.description = sb.toString();
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo);
        msg.setThumbImage(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    /*
     * 分享到微信好友
     * 分享到微信朋友圈
     */
    public static void share(Activity context, int flag) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, WXEntryActivity.APP_ID, true);
        api.registerApp(WXEntryActivity.APP_ID);

        if (!api.isWXAppInstalled()) {
            Utils.showToast(context, context.getResources().getString(R.string.client_not_installed));
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = "https://a.app.qq.com/o/simple.jsp?pkgname=" + BuildConfig.APPLICATION_ID;

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = context.getResources().getString(R.string.app_name);
        msg.description = "https://a.app.qq.com/o/simple.jsp?pkgname=" + BuildConfig.APPLICATION_ID;

        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo);

        msg.setThumbImage(thumb);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }

    private static boolean isSomething(String str) {
        return str != null && !str.trim().equals("");
    }
}
