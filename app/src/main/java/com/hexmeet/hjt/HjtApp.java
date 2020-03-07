package com.hexmeet.hjt;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.OrientationEventListener;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.MeizuRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.alibaba.sdk.android.push.register.OppoRegister;
import com.alibaba.sdk.android.push.register.VivoRegister;
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

import androidx.multidex.MultiDex;
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
        LOG.info("startFloatService, isStart? ["+isFloatServiceStart()+"]");
        if(!isFloatServiceStart) {
            Intent intent = new Intent(this, MeetingWindowService.class);
            setFloatServiceStart(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    public boolean isFloatServiceStart() {
        return isFloatServiceStart;
    }

    public void stopFloatService() {
        LOG.info("stopFloatService, isStart? ["+isFloatServiceStart()+"]");
        if(isFloatServiceStart) {
            setFloatServiceStart(false);
            Intent intent = new Intent(this, MeetingWindowService.class);
            stopService(intent);
        }
    }

    public void setFloatServiceStart(boolean floatServiceStart) {
        LOG.info("floatServiceStart, ["+floatServiceStart+"]");
        this.isFloatServiceStart = floatServiceStart;
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
                || topActivity.get() instanceof Conversation
        );
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
        // 初始化Mobile Analytics服务
        initManService();
        //阿里云推送
        initCloudChannel(this);
        floatWindowNotificationChannel(this);

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int normalHeap = activityManager.getMemoryClass();
        int largeHeap = activityManager.getLargeMemoryClass();
        LOG.info("max heap size=" + normalHeap + "M, larger heap size=" + largeHeap + "M");
        if(!SystemCache.getInstance().isUserMuteMic()){
            SystemCache.getInstance().setUserMuteMic(true);
        }

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

    private void initCloudChannel(Context context) {
        this.createNotificationChannel(context);
        PushServiceFactory.init(context);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(context, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                LOG.info("init cloudchannel success");
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                LOG.info("init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        MiPushRegister.register(context, BuildConfig.XIAOMI_ID, BuildConfig.XIAOMI_KEY);
        HuaWeiRegister.register(this);
        OppoRegister.register(context, BuildConfig.OPPO_KEY, BuildConfig.OPPO_APPSECRET);
        MeizuRegister.register(context, BuildConfig.MEIZU_ID, BuildConfig.MEIZU_KEY);
        VivoRegister.register(context);
    }


    private void initManService() {
        // 获取MAN服务
        MANService manService = MANServiceProvider.getService();
        // 打开调试日志
        manService.getMANAnalytics().turnOnDebug();
        manService.getMANAnalytics().setAppVersion("3.0");
        // 通过插件接入后直接在下发json中获取appKey和appSecret初始化
        manService.getMANAnalytics().init(this, getApplicationContext());
        // 通过此接口关闭页面自动打点功能
       // manService.getMANAnalytics().turnOffAutoPageTrack();
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


    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id
            String id = BuildConfig.NOTIFICATION;
            // 用户可以看到的通知渠道的名字.
            CharSequence name = getString(R.string.notification);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    public void floatWindowNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(BuildConfig.FLOATNOTIFICATION, getString(R.string.incall), NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);//无声音
            channel.enableVibration(false);//震动不可用
            manager.createNotificationChannel(channel);
        }
    }
}
