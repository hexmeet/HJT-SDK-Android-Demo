package com.hexmeet.hjt.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Utils {
    private static Logger log = Logger.getLogger(Utils.class);

   // private static Handler handler = new Handler();

    public static String getVersion() {
        PackageManager packageManager = HjtApp.getInstance().getContext().getPackageManager();
        String packageName = HjtApp.getInstance().getContext().getPackageName();
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (NameNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        return packageInfo != null ? packageInfo.versionName : "";
    }

    public static void showToast(Context context, int resourceId) {
        if (!HjtApp.isScreenLocked() && HjtApp.isForeground()) {
            SingleToast.showToast(context, resourceId);
        }
    }

    public static void showToast(Context context, String message) {
        if (!HjtApp.isScreenLocked() && HjtApp.isForeground()) {
            SingleToast.showToast(context, message);
        }
    }

    private static class SingleToast {
        private static Toast mToast;
        private static Handler cancelHandler = new Handler();
        private static Runnable cancelRunner = new Runnable() {
            @Override
            public void run() {
                mToast.cancel();
                mToast = null;
            }
        };

        public static void showToast(Context context, String text) {
            cancelHandler.removeCallbacks(cancelRunner);
            if (mToast != null) {
                mToast.setText(text);
            } else {
                mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            }

            cancelHandler.postDelayed(cancelRunner, 3000);
            mToast.show();
        }

        public static void showToast(Context context, int resId) {
            showToast(context, context.getResources().getString(resId));
        }
    }

    public static void showToastWithCustomLayout(Context context, final String message) {
        if (!HjtApp.isScreenLocked() && HjtApp.isForeground()) {
            SingleCenterToast.showToast(context, message);
        }
    }

    private static class SingleCenterToast {
        private static Toast mToast;
        private static Handler cancelHandler = new Handler();
        private static Runnable cancelRunner = new Runnable() {
            @Override
            public void run() {
                mToast.cancel();
                mToast = null;
            }
        };

        public static void showToast(Context context, String message) {
            cancelHandler.removeCallbacks(cancelRunner);
            View layout = LayoutInflater.from(context).inflate(R.layout.my_toast, null);
            TextView text = (TextView) layout.findViewById(R.id.message);
            text.setText(message);
            if (mToast == null) {
                mToast = new Toast(context);
            }
            mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setView(layout);

            cancelHandler.postDelayed(cancelRunner, 3000);
            mToast.show();
        }
    }

    private static class ToastThread extends Thread {
        private Context context;
        private Handler mHandler;
        private Looper mLooper;
        private ReentrantLock lock = new ReentrantLock();
        private Condition condition = lock.newCondition();

        public ToastThread(Context context) {
            this.context = context;
            start();
        }

        @Override
        public void run() {
            log.info("...toast thread started...");
            Looper.prepare();
            mLooper = Looper.myLooper();
            mHandler = new Handler(mLooper) {
                @Override
                public void handleMessage(Message message) {
                    Toast.makeText(context, (String) message.obj, Toast.LENGTH_SHORT).show();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            quit();
                        }
                    }, 5000);
                }
            };

            try {
                lock.lock();
                condition.signal();
            } finally {
                lock.unlock();
            }
            Looper.loop();
            log.info("...toast thread quitted...");
        }

        public void show(String message) {
            try {
                lock.lock();
                while (mLooper == null || mHandler == null) {
                    condition.await();
                }

                Message msg = Message.obtain();
                msg.obj = message;
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }

        private void quit() {
            if (mLooper != null) {
                mLooper.quit();
            }
        }
    }

    public static boolean isForground() {
        Context context = HjtApp.getInstance().getContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses != null) {
            for (RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    }

                    return false;
                }
            }
        }

        return false;
    }

    public static boolean copyFile(File src, File dst) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (inChannel != null)
                try {
                    inChannel.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

            if (outChannel != null)
                try {
                    outChannel.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
        }
        return false;
    }

    public static String getUUID() {
        String s = UUID.randomUUID().toString();
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
    }

    public static void loadAvatar(ImageView imageView) {
        if ( SystemCache.getInstance().getLoginResponse() == null || SystemCache.getInstance().getDownloadUserImage()==null) {
            log.error("loadAvatar error: no login response");
            return;
        }

        File file = new File(SystemCache.getInstance().getDownloadUserImage());
        Log.i("ImageViewPath",SystemCache.getInstance().getDownloadUserImage()+"===="+file);
        if(file.exists()) {
            Picasso.with(HjtApp.getInstance().getContext()).load(file).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).networkPolicy(NetworkPolicy.NO_CACHE).into(imageView);
        } else {
            log.error("loadAvatar error: no userImage nothing");
        }
    }

    public static void copyFile(File srcFile, String targetPath) {
        try {
            int byteread = 0;
            if (srcFile.exists()) {
                InputStream inStream = new FileInputStream(srcFile);
                FileOutputStream fs = new FileOutputStream(targetPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            log.error("copyFile: " + e.getMessage(), e);
        }

    }

}
