package com.hexmeet.hjt.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;

import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class NetworkUtil extends android.support.v4.app.FragmentActivity {
    private static Logger LOG = Logger.getLogger(NetworkUtil.class);

    private static boolean cloudAlive = true;
    private static final int UCM_DETECTOR_DELAY_IN_SECOND = 8;
    private static ScheduledExecutorService serviceCloudDetector = null;
    private static ScheduledFuture<?> detectorTask = null;

    public static boolean isInternalNet(Context context) {
        if (NetworkUtil.isNetConnected(context)) {
            if (NetworkUtil.isWifiConnected(context)) {
                return false;
            } else if (NetworkUtil.is3GConnected(context)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNetConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo[] infos = cm.getAllNetworkInfo();
                if (infos != null) {
                    for (NetworkInfo ni : infos) {
                        if (ni.isConnected()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null &&
                        (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean is3GConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPortReachable(String host, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 2000);
            socket.close();
            return true;
        } catch (Throwable e) {
            LOG.error("NetworkUtil - isPortReachable failed to connect to " + host + ":" + port + ", reason: " + e.getMessage());
            return false;
        }
    }

    public static boolean isCloudReachable(Context context) {
        if (!SystemCache.getInstance().isNetworkConnected()) {
            LOG.warn("App.isNetworkConnected()=false");
            Utils.showToast(context, R.string.network_unconnected);
            return false;
        }

        if (!cloudAlive) {
            LOG.warn("cloudAlive=false");
            Utils.showToast(context, R.string.cloud_unreachable);
            return false;
        }

        return true;
    }

    public static boolean isCloudReachable() {
        if (!SystemCache.getInstance().isNetworkConnected()) {
            LOG.warn("App.isNetworkConnected()=false");
            return false;
        }

        if (!cloudAlive) {
            LOG.warn("cloudAlive=false");
            return false;
        }

        return true;
    }

    public static void shutdown() {
        if (detectorTask != null) {
            detectorTask.cancel(true);
            detectorTask = null;
        }
        if (serviceCloudDetector != null) {
            serviceCloudDetector.shutdownNow();
            serviceCloudDetector = null;
        }
        cloudAlive = true;
    }
}
