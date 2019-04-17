package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().toString();
    private static final int MEMORY_LOG_FILE_MAX_SIZE = 4 * 1024 * 1024;
    private static CrashHandler mInstance = new CrashHandler();

    private Logger log = Logger.getLogger(this.getClass());

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return mInstance;
    }

    @SuppressWarnings("static-access")
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (isOOM(ex)) {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
            String date = sDateFormat.format(new Date());
            File dumpfile = new File(SDCARD_ROOT + File.separator + "crash" + File.separator + "oom_" + date
                    + ".hprof");
            try {
                Debug.dumpHprofData(dumpfile.getAbsolutePath());
            } catch (IOException e) {
            }
        }

        saveInfoToSD(HjtApp.getInstance().getContext(), ex);
        //Utils.showToastInNewThread(HjtApp.getInstance().getContext(), HjtApp.getInstance().getResources().getString(R.string.caught_exception_then_exit));

        try {
            thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(1);

        System.exit(1);

        // ExitAppUtils.getInstance().exit();
    }

    public boolean isOOM(Throwable throwable) {
        if ("java.lang.OutOfMemoryError".equalsIgnoreCase(throwable.getClass().getName())) {
            return true;
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            return isOOM(cause);
        }
        return false;
    }

    public void setCustomCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private HashMap<String, String> obtainSimpleInfo(Context context) {
        HashMap<String, String> map = new HashMap<>();
        PackageManager mPackageManager = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = mPackageManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        map.put("versionName", mPackageInfo.versionName);
        map.put("versionCode", "" + mPackageInfo.versionCode);

        map.put("MODEL", "" + Build.MODEL);
        map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
        map.put("PRODUCT", "" + Build.PRODUCT);

        return map;
    }

    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter mStringWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mStringWriter);
        throwable.printStackTrace(mPrintWriter);
        mPrintWriter.close();

        log.error(mStringWriter.toString());
        return mStringWriter.toString();
    }

    private String saveInfoToSD(Context context, Throwable ex) {
        String fileName = null;
        StringBuffer sb = new StringBuffer();

        for (Map.Entry<String, String> entry : obtainSimpleInfo(context).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }

        sb.append(obtainExceptionInfo(ex));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(SDCARD_ROOT + File.separator + "crash" + File.separator);
            fileName = dir.toString() + File.separator + "hjt_crash.log";

            if (!dir.exists()) {
                dir.mkdir();
            } else {
                File[] tempList = dir.listFiles();
                for (int i = 0; i < tempList.length; i++) {
                    if (tempList[i].isFile()) {
                        if (tempList[i].getName().contains("hjt_crash.log")) {
                            fileName = dir.toString() + File.separator + "hjt_crash.log";
                            File file = new File(fileName);
                            if (file.exists()) {
                                if (file.length() >= MEMORY_LOG_FILE_MAX_SIZE) {
                                    String newname = dir.toString() + File.separator + "hjt_crash.log1";
                                    File newfile = new File(newname);
                                    if (newfile.exists()) {
                                        newfile.delete();
                                    }
                                    renameFile(fileName, newname);
                                    fileName = dir.toString() + File.separator + "hjt_crash.log";
                                }
                            }
                            break;
                        }
                    }
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(fileName, true);
                fos.write((parserTime(System.currentTimeMillis()) + "\n").getBytes());
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        return fileName;

    }

    private String parserTime(long milliseconds) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times = format.format(new Date(milliseconds));

        return times;
    }

    private void renameFile(String oldname, String newname) {
        if (!oldname.equals(newname)) {
            File oldfile = new File(oldname);
            File newfile = new File(newname);
            if (!oldfile.exists()) {
                log.error("ordfile is not exists");
                return;
            }
            if (newfile.exists())
                log.error("newfile is already exists");
            else {
                oldfile.renameTo(newfile);
            }
        }
    }
}
