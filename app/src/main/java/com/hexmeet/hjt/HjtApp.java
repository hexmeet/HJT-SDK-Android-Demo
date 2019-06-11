package com.hexmeet.hjt;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.view.OrientationEventListener;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.CallIncomingActivity;
import com.hexmeet.hjt.call.ConnectActivity;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.service.AppService;
import com.hexmeet.hjt.service.MeetingWindowService;
import com.hexmeet.hjt.utils.ConfigureLog4J;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Locale;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class HjtApp extends Application {
    private static HjtApp instance;
    private AppService appService;
    private Logger LOG = Logger.getLogger(HjtApp.class);
    private Activity conversation;
    private WeakReference<Activity> topActivity;
    private boolean isFloatServiceStart = false;

    public synchronized static HjtApp getInstance() {
        while (instance == null) {
            try {
                HjtApp.class.wait();
            } catch (InterruptedException e) {
                break;
            }
        }
        return instance;
    }

    public void startFloatService() {
        LOG.info("startFloatService, isStart? ["+isFloatServiceStart+"]");
        if(!isFloatServiceStart) {
            Intent intent = new Intent(this, MeetingWindowService.class);
            setFloatServiceStart(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
           // startService(intent);
        }
    }

    public boolean isFloatServiceStart() {
        return isFloatServiceStart;
    }

    public void stopFloatService() {
        LOG.info("stopFloatService, isStart? ["+isFloatServiceStart+"]");
        if(isFloatServiceStart) {
            setFloatServiceStart(false);
            Intent intent = new Intent(this, MeetingWindowService.class);
            stopService(intent);

        }
    }

    public void setFloatServiceStart(boolean floatServiceStart) {
        LOG.info("floatServiceStart, ["+floatServiceStart+"]");
        isFloatServiceStart = floatServiceStart;
    }

    public void bindAppService() {
        Intent intent = new Intent(getContext(), AppService.class);
        bindService(intent, connection, BIND_AUTO_CREATE | BIND_ABOVE_CLIENT);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOG.info("Bind AppService: onServiceConnected");
            appService = ((AppService.AppServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOG.info("Bind AppService: onServiceDisconnected");
            appService = null;
            bindAppService();
        }
    };

    public AppService getAppService() {
        return appService;
    }

    public static boolean isEnVersion() {
        return !Locale.getDefault().getLanguage().equals("zh");
    }

    public static boolean isCnVersion() {
        return Locale.getDefault().getLanguage().equals("zh");
    }

    private static boolean isForeground = true;

    public static boolean isForeground() {
        return isForeground;
    }

    private static boolean isScreenLocked = false;

    public static boolean isScreenLocked() {
        return isScreenLocked;
    }

    public static void setScreenLocked(boolean locked) {
        HjtApp.isScreenLocked = locked;
    }

    private static boolean isGSMCalling = false;

    public static synchronized boolean isGSMCalling() {
        return isGSMCalling;
    }

    public static synchronized void setGSMCalling(boolean isMute) {
        isGSMCalling = isMute;
    }


    public Activity getTopActivity() {
        if (topActivity != null) {
            return topActivity.get();
        }

        return null;
    }

    public boolean isCalling() {
        return topActivity != null && (topActivity.get() instanceof CallIncomingActivity
                || topActivity.get() instanceof ConnectActivity
                || topActivity.get() instanceof Conversation);
    }

    private static boolean isSpeakerOn = false;

    public static synchronized void setSpeakerOn(boolean yesno) {
        isSpeakerOn = yesno;
    }

    public static synchronized boolean isSpeakerOn() {
        return isSpeakerOn;
    }

    private OrientationEventListener orientationListener = null;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MANService manService = MANServiceProvider.getService();

        LOG.info("App - onCreate");
        synchronized (HjtApp.class) {
            instance = this;
        }

        LOG.info("APP Process Name is " + this.getProcessName());
        if(isAppProcess()){
            LOG.info("Start bindAppService");
            bindAppService();
        }

        SystemCache.getInstance().setNetworkConnected(NetworkUtil.isNetConnected(getContext()));

        manService.getMANAnalytics().init(this, getApplicationContext());


        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int normalHeap = activityManager.getMemoryClass();
        int largeHeap = activityManager.getLargeMemoryClass();
        LOG.info("max heap size=" + normalHeap + "M, larger heap size=" + largeHeap + "M");

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStopped(Activity activity) {
                isForeground = Utils.isForground();
            }

            @Override
            public void onActivityStarted(Activity activity) {
                isForeground = true;
                topActivity = new WeakReference<>(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if(!activity.equals(topActivity.get())) {
                    isForeground = true;
                    topActivity = new WeakReference<>(activity);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }
        });

        if (orientationListener == null) {
            orientationListener = new OrientationEventListener(HjtApp.this) {
                private int oldDirection = 0;
                private int oldCameraDirection = -1;

                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                        return;
                    }

                    int direction = 0;
                    if ((orientation >= 0 && orientation <= 40)
                            || (orientation >= 320 && orientation <= 360)) {
                        direction = 0;
                    } else if (orientation >= 50 && orientation <= 130) {
                        direction = 90;
                    } else if (orientation >= 140 && orientation <= 220) {
                        direction = 180;
                    } else if (orientation >= 230 && orientation <= 310) {
                        direction = 270;
                    } else {
                        return;
                    }

                    if (Math.abs(oldDirection - orientation) >= 50 && direction != oldDirection) {
                        onNewDirection(direction);
                        oldDirection = direction;

                    }
                }

                private void onNewDirection(final int direction) {
                    if (direction != oldCameraDirection) {
                        final int _cameraDirection = (360 - direction) % 360;
                        if(appService!=null){
                            appService.cameraDirection(_cameraDirection);
                            oldCameraDirection = direction;
                        }


                    }
                }
            };

            orientationListener.enable();
        }
    }

    public void initLogs() {
        if (ConfigureLog4J.getLogConfigurator() == null) {
            ConfigureLog4J.setLogConfigurator(new LogConfigurator());
            ConfigureLog4J.configure();
        }
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.setCustomCrashHandler();
    }

    @Override
    public void onTerminate() {
        LOG.info("App - onTerminate");
        super.onTerminate();
        if (appService != null) {
            appService.releaseSdk();
        }

    }

    public void setConversation(Activity conversation) {
        this.conversation = conversation;
    }

    public void checkCallScreen() {
        if(conversation != null && !conversation.isFinishing()) {
            conversation.finish();
        }
    }

    public Context getContext() {
        return getApplicationContext();
    }

    /**
     * 判断该进程是否是app进程
     * @return
     */
    public boolean isAppProcess() {
        String processName = getProcessName();
        LOG.info("App - processName : "+ processName);
        if (processName == null || !processName.equalsIgnoreCase(this.getPackageName())) {
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获取运行该方法的进程的进程名
     * @return 进程名称
     */


    public static   String getProcessName() {
        int processId = android.os.Process.myPid();
        String processName = null;
        ActivityManager manager = (ActivityManager) getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        Iterator iterator = manager.getRunningAppProcesses().iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningAppProcessInfo processInfo = (ActivityManager.RunningAppProcessInfo) (iterator.next());
            try {
                if (processInfo.pid == processId) {
                    processName = processInfo.processName;
                    return processName;
                }
            } catch (Exception e) {
//                LogD(e.getMessage())
            }
        }
        return processName;
    }
}
