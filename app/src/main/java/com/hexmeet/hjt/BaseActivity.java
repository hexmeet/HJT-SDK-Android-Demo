package com.hexmeet.hjt;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.conf.MeetingForWechat;
import com.hexmeet.hjt.conf.WeChat;
import com.hexmeet.hjt.utils.JsonUtil;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.StateUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

public class BaseActivity extends android.support.v4.app.FragmentActivity {
    public Logger LOG = Logger.getLogger(this.getClass());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.info("onCreate()");
        StateUtil.setStatusBar(this,true,false);
        StateUtil.setStatusTextColor(true,this);
        super.onCreate(savedInstanceState);
        SystemCache.getInstance().setNetworkConnected(NetworkUtil.isNetConnected(this));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LOG.info("onNewIntent()");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        LOG.info("onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        LOG.info("onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        LOG.info("onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        LOG.info("onResume()");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LOG.info("onDestroy()");
        super.onDestroy();
    }

    public void shareToWechat(final String json) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(json)) {
                    Utils.showToast(BaseActivity.this, R.string.empty_share);
                    return;
                }
                try {
                    MeetingForWechat meeting = JsonUtil.toObject(json, MeetingForWechat.class);
                    WeChat.share(BaseActivity.this, meeting);
                } catch (Exception e) {
                    LOG.error("shareToWechat: "+ e.getMessage(), e);
                    Utils.showToast(BaseActivity.this, R.string.share_failed);
                }
            }
        });
    }

    public void shareToEmail(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(content)) {
                    Utils.showToast(BaseActivity.this, R.string.empty_share);
                    return;
                }
                try {
//                    String[] email = {"appsupport@cninnovatel.com"};
                    String[] email = {};
                    String uriString = "mailto:";
                    final String SUBJECT = "subject=";
                    String message = content.toString();
                    String emailBody = "";
                    if(message.startsWith(uriString) && message.contains("&body=")) {
                        String[] strs = message.split("&body=");
                        uriString = strs[0];
                        if(strs.length > 1) {
                            emailBody = strs[1];
                        }
                    }

                    String subject = getString(R.string.share_meeting);
                    if(message.indexOf(SUBJECT) > 0) {
                        subject = message.substring(message.indexOf(SUBJECT) + SUBJECT.length(),message.indexOf("&"));
                    }
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE, uri);
                    intent.putExtra(Intent.EXTRA_EMAIL, email); // 接收人
                    intent.putExtra(Intent.EXTRA_CC, ""); // 抄送人
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject); // 主题

                    String model = Build.BRAND;
                    if(model.equalsIgnoreCase("xiaomi")) {
                        emailBody = emailBody.replace("\n","<br>");
                        intent.setType("text/html");
                    }else {
                        intent.setType("text/plain");
                    }
                    intent.putExtra(Intent.EXTRA_TEXT, emailBody); // 正文
                    startActivity(Intent.createChooser(intent, getString(R.string.select_email_software)));
                } catch (Exception e) {
                    LOG.error("shareToEmail: "+ e.getMessage(), e);
                    Utils.showToast(BaseActivity.this, R.string.share_failed);
                }
            }
        });
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            Configuration configuration = resources.getConfiguration();
            configuration.fontScale =1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }

}
