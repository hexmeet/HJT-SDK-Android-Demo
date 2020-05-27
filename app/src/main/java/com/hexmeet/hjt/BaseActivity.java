package com.hexmeet.hjt;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.conf.MeetingForWechat;
import com.hexmeet.hjt.conf.WeChat;
import com.hexmeet.hjt.me.VersionState;
import com.hexmeet.hjt.service.UpgradeService;
import com.hexmeet.hjt.utils.JsonUtil;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.StateUtil;
import com.hexmeet.hjt.utils.UpdateVersionUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.fragment.app.FragmentActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BaseActivity extends FragmentActivity {
    public Logger LOG = Logger.getLogger(this.getClass());
    private UpdateVersionUtil dialog;
    final  int HANDLER_UPDATED_VERSION = 1001;
    final  int HANDLER_VERSION_TOAST = 1002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.info("onCreate()");
        StateUtil.setStateBarUtil(BaseActivity.this);
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

    public void checkVersion(final boolean isOnclick){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemCache.getInstance().setShowVersionDialog(false);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(BuildConfig.APPINFO_URL).build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            if(response.isSuccessful()){
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    String platform = (String) jsonObject.get("PLATFORM");
                                    final String version = (String) jsonObject.get("VERSION");
                                    final String download_url = (String) jsonObject.get("DOWNLOAD_URL");
                                    if(platform!=null && platform.equals("android")){
                                        LOG.info("APP version : "+Utils.getVersion()+",API response verstion : "+version+",compareVersion :"+Utils.compareVersion(version, Utils.getVersion())==1+"");

                                        if (Utils.compareVersion(version, Utils.getVersion())==1) {
                                            Message msg = Message.obtain();
                                            msg.what = HANDLER_UPDATED_VERSION;
                                            Bundle bundle = new Bundle();
                                            bundle.putString("version",version);
                                            bundle.putString("download_url",download_url);
                                            msg.setData(bundle);
                                            handler.sendMessage(msg);
                                        }else {
                                            if (isOnclick) {
                                                handler.sendEmptyMessage(HANDLER_VERSION_TOAST);
                                            }
                                        }

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                });

            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==HANDLER_UPDATED_VERSION){
                String version = msg.getData().getString("version");
                String downloadUrl = msg.getData().getString("download_url");
                showVersionDialog(version,downloadUrl);
            }else if(msg.what==HANDLER_VERSION_TOAST){
                Utils.showToast(BaseActivity.this, R.string.version);
            }
        }
    };

    private void showVersionDialog(String version,String download_url) {
        dialog = new UpdateVersionUtil.Builder(this)
                .setCancelButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SystemCache.getInstance().setShowRemind(true);
                        onNotice(true);
                        dialog.dismiss();
                    }
                }).setOkButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SystemCache.getInstance().setShowRemind(false);
                        onNotice(false);
                        Intent intent = new Intent(BaseActivity.this, UpgradeService.class);
                        intent.putExtra(AppCons.APK_URL, download_url);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                        Utils.showToast(BaseActivity.this,R.string.new_version);
                        dialog.dismiss();
                    }
                }).setVersion(version).createTwoButtonDialog();
        dialog.show();
    }

    private void onNotice(boolean show){
        if(show){
            EventBus.getDefault().post(VersionState.VISIBLE_VERSIONIMG);
        }else {
            EventBus.getDefault().post(VersionState.INVISIBLE_VERSIONIMG);
        }

    }

}
