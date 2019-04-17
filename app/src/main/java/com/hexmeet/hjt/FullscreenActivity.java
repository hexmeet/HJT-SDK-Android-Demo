package com.hexmeet.hjt;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.login.LoginService;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.sdk.CopyAssets;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static android.media.AudioManager.STREAM_MUSIC;

public class FullscreenActivity extends Activity {
    private Logger LOG = Logger.getLogger(this.getClass());
    static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private MediaPlayer mRingerPlayer;

    public String getSDCardRoot() {
        return SDCardRoot;

    }

    public AssetManager getAssetManager() {
        return getResources().getAssets();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = getWindow();
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void startRinging() {
        try {
            LOG.debug("Start Ringing");
             String mRingSoundFile = CopyAssets.getInstance().getRingBasePath() + "/ringtone.wav";
            Log.i("mRingSoundFile : ",mRingSoundFile);
            CopyAssets.getInstance().copyIfNotExist(R.raw.ringtone, mRingSoundFile);
            if (mRingerPlayer == null) {
                mRingerPlayer = new MediaPlayer();
                mRingerPlayer.setAudioStreamType(STREAM_MUSIC);
                try {
                    String ringtone = mRingSoundFile;
                    if (ringtone.startsWith("content://")) {
                        mRingerPlayer.setDataSource(this, Uri.parse(ringtone));
                    } else {
                        FileInputStream fis = new FileInputStream(ringtone);
                        mRingerPlayer.setDataSource(fis.getFD());
                        fis.close();
                    }
                } catch (IOException e) {
                    LOG.error("Cannot set ringtone", e);
                }

                mRingerPlayer.prepare();
                mRingerPlayer.setLooping(true);
                mRingerPlayer.start();
            }
        } catch (Exception e) {
            LOG.error("cannot handle incoming call", e);
        }
    }

    protected void stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
       String event = CopyAssets.REMOTE_RING_EVENT;
        int value = CopyAssets.EVENT_STOP;
        CopyAssets.getInstance().processAudioRouteEvent(event, value);
    }

    protected void clearAnonymousData() {
        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, true);
        SystemCache.getInstance().resetAnonymousLoginCache();
        HjtApp.getInstance().getAppService().setUserInLogin(false);
    }

    protected void clearInviteData(){
        Log.i("fullscreen",SystemCache.getInstance().isInviteMakeCall()+"");
        // TODO - why make a autoLogin here?
        LoginService.getInstance().autoLogin();
        SystemCache.getInstance().setInviteMakeCall(false);
    }

    protected void hideNavigationBar(final Handler handler) {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                handler.postDelayed(mHideRunnable, 2000); // hide the navigation bar
            }
        });
    }

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            int flags;
            int curApiVersion = android.os.Build.VERSION.SDK_INT;
            // This work only for android 4.4+
            if (curApiVersion >= Build.VERSION_CODES.KITKAT) {
                // This work only for android 4.4+
                // hide navigation bar permanently in android activity
                // touch the screen, the navigation bar will not show
                flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

            } else {
                // touch the screen, the navigation bar will show
                flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            // must be executed in main thread :)
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    };
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
