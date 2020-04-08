package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.sdk.CopyAssets;

import org.apache.log4j.Logger;

public class PhoneStateChangedReceiver extends BroadcastReceiver {
    private Logger LOG = Logger.getLogger(PhoneStateChangedReceiver.class);
    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {

        final String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        LOG.info("hexmeet PhoneStateChangedReceiver : "+extraState);
        //来电或者电话中
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(extraState) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(extraState)) {
            HjtApp.setGSMCalling(true);
            if (!CopyAssets.isInstanciated()) {
                LOG.info("GSM call state changed but manager not instantiated");
                return;
            }
            LOG.info("audioInterruption 1");
            if(HjtApp.getInstance().getAppService()!=null){
                HjtApp.getInstance().getAppService().enableSpeaker(false);
                LOG.info("call recevied. mute video");
                HjtApp.getInstance().getAppService().phoneStateChange(true);
                LOG.info("micEnabled : "+HjtApp.getInstance().getAppService().micEnabled());
                if(TelephonyManager.EXTRA_STATE_RINGING.equals(extraState)){
                    SystemCache.getInstance().setMuteMic(HjtApp.getInstance().getAppService().micEnabled());
                    HjtApp.getInstance().getAppService().muteMic(true);
                }
            }
            //电话空闲
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(extraState)) {
            LOG.info("resume GSM");
            HjtApp.setGSMCalling(false);
            if(HjtApp.getInstance().getAppService()!=null){
                LOG.info("call hunged. App.IsUserMuteVideo: " + SystemCache.getInstance().isMuteMic());
                if(SystemCache.getInstance().isMuteMic()) {
                    HjtApp.getInstance().getAppService().muteMic(false);
                } else {
                    HjtApp.getInstance().getAppService().muteMic(true);//video mute
                }
                SystemCache.getInstance().setMuteMic(false);
                LOG.info("audioInterruption 0, isSpeakerOn: " + HjtApp.isSpeakerOn());
                HjtApp.getInstance().getAppService().enableSpeaker(true);
                HjtApp.getInstance().getAppService().phoneStateChange(false);
            }


                if(CopyAssets.getInstance().isBluetoothConnected()){
                    LOG.info(" route to Bluetooth");
                    Handler postHandler = new Handler();
                    postHandler.postDelayed(new Runnable(){
                        public void run(){
                            LOG.info(" post run: route to Bluetooth");
                            CopyAssets.getInstance().routeAudioToBluetooth();
                        }
                    }, 500);
                }
                else {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    if(am.isWiredHeadsetOn()){
                        //earphone
                        LOG.info("hexmeet PhoneStateChang edReceiver   route to receiver-earphone");
                        CopyAssets.getInstance().routeAudioToReceiver();
                    } else {
                        LOG.info("hexmeet PhoneStateChangedReceiver   route to speaker");
                        CopyAssets.getInstance().routeAudioToSpeaker();
                    }
                }

        }

    }
}

