package com.hexmeet.hjt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class PermissionWrapper {
    public Logger LOG = Logger.getLogger(this.getClass());
    public static final int REQUEST_PERMISSIONS_STORAGE = 111;
    public static final int REQUEST_PERMISSIONS_MEETING = 112;
    public static final int REQUEST_PERMISSIONS_HEXMEET = 113;

    public static final int REQUEST_PERMISSIONS_FLOAT_WINDOW = 1100;

    public static final int RESULT_PERMISSIONS_NOTHING = 11;
    public static final int RESULT_PERMISSIONS_PASS = 12;
    public static final int RESULT_PERMISSIONS_REJECT = 13;

    private boolean askPermission = false;
    private static PermissionWrapper instance;

    private PermissionWrapper() {
    }

    public static PermissionWrapper getInstance() {
        if(instance == null) {
            instance = new PermissionWrapper();
        }
        return instance;
    }

    private String[] PERMISSIONS_MEETING = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };


    public boolean checkStoragePermission(Activity activity) {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_PERMISSIONS_STORAGE);
            askPermission = true;
            return false;
        }
        return true;
    }

    public boolean checkMeetingPermission(Activity activity) {
        return checkMeetingPermission(activity, REQUEST_PERMISSIONS_MEETING);
    }

    public boolean checkHexMeetPermission(Activity activity) {
        return checkMeetingPermission(activity, REQUEST_PERMISSIONS_HEXMEET);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasFloatWindowPermission(Activity activity, boolean turnToSettings) {
//        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity, Manifest.permission.SYSTEM_ALERT_WINDOW);
        boolean ok = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(HjtApp.getInstance().getContext());
        if(!ok && turnToSettings) {
            Utils.showToast(activity, activity.getString(R.string.need_float_window_permission, activity.getString(R.string.app_name)));
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_PERMISSIONS_FLOAT_WINDOW);
        }
        return ok;
    }

    private boolean checkMeetingPermission(Activity activity, int requestCode) {
        ArrayList<String> noPass = new ArrayList<>();
        for (String p : PERMISSIONS_MEETING) {
            if(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, p)) {
                noPass.add(p);
            }
        }
        if(noPass.isEmpty()) {
            LOG.info("All Permissions GRANTED");
            return true;
        } else {
            String[] requestPermission = new String[noPass.size()];
            noPass.toArray(requestPermission);
            LOG.info("Still have ["+requestPermission.length+"] Permissions not GRANTED");
            ActivityCompat.requestPermissions(activity, requestPermission, requestCode);
            askPermission = true;
            return false;
        }
    }

    public int processRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSIONS_STORAGE || requestCode == REQUEST_PERMISSIONS_MEETING || requestCode == REQUEST_PERMISSIONS_HEXMEET) {
            LOG.info("Ask before? ["+askPermission+"],Return Permissions grantResults : " + Arrays.toString(grantResults));

            if(!askPermission) {
                return RESULT_PERMISSIONS_NOTHING;
            }

            if(checkPermissionResult(grantResults)) {
                if(requestCode == REQUEST_PERMISSIONS_STORAGE) {
                    HjtApp.getInstance().initLogs();
                } else {
                    HjtApp.getInstance().getAppService().reloadHardware();
                }
                return RESULT_PERMISSIONS_PASS;
            } else {
                if(requestCode == REQUEST_PERMISSIONS_STORAGE) {
                    LOG.error("Read & Write SD card PERMISSION declined, shut down app");
                } else {
                    LOG.error("Camera & audio & location & phone state ,  PERMISSION declined can not make call");
                }
                return RESULT_PERMISSIONS_REJECT;
            }
        }
        return RESULT_PERMISSIONS_NOTHING;
    }

    private boolean checkPermissionResult(int[] grantResults)  {
        boolean pass = true;

        for(int r : grantResults) {
            if(r != 0) {
                pass = false;
                break;
            }
        }
        return pass;
    }
}
