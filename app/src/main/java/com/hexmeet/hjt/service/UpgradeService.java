package com.hexmeet.hjt.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

public class UpgradeService extends Service {

    private Logger LOG = Logger.getLogger(UpgradeService.class);
    private String mUpgradeAddress;

    private int mDownloadProgress = 0;
    private String mSDCardPath;
    private File mInstallPath;
    private NotificationManager mNotificationManger;
    private Notification mNotification;
    private Notification.Builder builder;

    public static final int PROGRESS=10;
    public static final int NETWORK_FAIL=11;
    public static final int NOTITY_ID =12;
    public static final int INSTALL_APK = 13;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LOG.info("onCreate()");
        mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mNotificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mUpgradeAddress = intent.getStringExtra(AppCons.APK_URL);
        String fileName = mUpgradeAddress.substring(mUpgradeAddress.lastIndexOf("/") + 1);
        mInstallPath = new File(mSDCardPath, fileName);
        LOG.info("mInstallPath : "+ mInstallPath.getAbsolutePath());
        mDownloadProgress = 0;
        createDownloadNotity();

        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadAPK();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }


    @SuppressLint("NewApi")
    private void createDownloadNotity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, BuildConfig.FLOATNOTIFICATION)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(HjtApp.getInstance().getString(R.string.app_name))
                    .setProgress(100,0,false);
            //.setTicker("开始下载");
            mNotification = builder.build();
            startForeground(NOTITY_ID, mNotification);
        }else {
            builder = new Notification.Builder(this)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(HjtApp.getInstance().getString(R.string.app_name))
                    .setProgress(100,0,false);
            mNotification = builder.build();
            mNotificationManger.notify(NOTITY_ID, mNotification);
        }



    }


    private void downloadAPK() {
        try {
            LOG.info("mUpgradeAddress : "+mUpgradeAddress);
            URL url = new URL(mUpgradeAddress);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if (200 != httpURLConnection.getResponseCode()) {
                mHandler.sendEmptyMessage(NETWORK_FAIL);
                return;
            }
            int fileSize = httpURLConnection.getContentLength();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(mInstallPath));

            byte[] buffer = new byte[1024 * 50];
            int len;
            double readFileLen = 0;
            int lastProgress = 0;

            while (-1 != (len = inputStream.read(buffer))) {
                bufferedOutputStream.write(buffer, 0, len);
                bufferedOutputStream.flush();
                readFileLen += len;
                mDownloadProgress = (int) (readFileLen / fileSize * 100);

                if (mDownloadProgress > lastProgress) {
                    Message msg = new Message();
                    msg.what = PROGRESS;
                    msg.arg1 = mDownloadProgress;
                    mHandler.sendMessage(msg);
                    lastProgress = mDownloadProgress;
                }

            }
            bufferedOutputStream.close();
            inputStream.close();

            mHandler.sendEmptyMessage(INSTALL_APK);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LOG.info("handleMessage : "+msg.what);
            if (msg.what == NETWORK_FAIL) {
                Utils.showToast(getApplicationContext(), R.string.server_unavailable);
            } else if (msg.what == PROGRESS) {
                int progress = msg.arg1;
                builder.setContentText(getString(R.string.downloaded)+progress + "%");
                builder.setProgress(100, progress, false);
                mNotificationManger.notify(NOTITY_ID, builder.build());
            } else if (msg.what == INSTALL_APK) {
                installApk();
                stopSelf();
                mNotificationManger.cancel(NOTITY_ID);
            }
        }
    };

    private void installApk() {
        if (!mInstallPath.exists()) {
            LOG.info("Installation package path does not exist");
            return;
        }
        LOG.info("Download complete  : "+ mInstallPath.getAbsolutePath());
        String absolutePath = mInstallPath.getAbsolutePath();

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LOG.info("path : "+mInstallPath.getAbsolutePath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(this, AppCons.APP_FILE_PROVIDER_AUTH,  new File(absolutePath));
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(contentUri, "application/vnd.android.package-archive");
        }else {
            i.setDataAndType(Uri.fromFile(new File(absolutePath)), "application/vnd.android.package-archive");
        }
        startActivity(i);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
