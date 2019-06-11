package com.hexmeet.hjt.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CopyAssets {
    private Logger LOG = Logger.getLogger(CopyAssets.class);
    public Context mServiceContext;
    public   String mBackgroundFile;
    public   String mBackgroundCallingFile;
    public   String mUserFile;
    private  String mErrorToneFile;
    private  String mRootCaFile;
    private Resources mR;
    private String basePath;
    private  String mUserCertificatePath;
    private static CopyAssets instance ;
    private AudioManager mAudioManager;

    private boolean isBTPHeadSetConnected = false;
    private boolean mCountDownTimerStarted = false;

    public static final int EVENT_START = 1;
    public static final int EVENT_STOP = 0;
    public static final String PHONE_INTERRUPTION = "event.phoneInterruption";
    public static final String HEADSET_PLUG_EVENT = "event.headsetPlug";
    public static final String BLUETOOTH_CONNECTION_EVENT = "event.bluetoothConnection";
    public static final String CONVERSATION_EVENT = "event.conversation";
    public static final String CONVERSATION_AUDIOONLY_EVENT = "event.conversation.audioonly";
    public static final String INCOMING_RING_EVENT = "event.incomingRing";
    public static final String REMOTE_RING_EVENT = "event.remoteRing";
    public static final String UI_SPEAKERLABEL_EVENT = "event.uiSpeakerLable";
    public static final String BLUETOOTH_CONNECTION_FAILED = "event.bluetoothConnectionFailed";

    public static final String ROUTE_TO_SPEAKER = "route.speaker";
    public static final String ROUTE_TO_RECEIVER = "route.receiver";
    public static final String ROUTE_TO_WIREDHEADSET = "route.wiredHeadset";
    public static final String ROUTE_TO_BLUETOOTH = "route.bluetooth";

    private int mCurrentAudioMode = AudioManager.MODE_CURRENT;
    private String mCurrentAudioRoute = ROUTE_TO_SPEAKER;
    private int mlastHeadset = NO_HEADSET;

    private static final int NO_HEADSET = -1;
    private static final int BLUETOOTH_HEADSET = 0;
    private static final int WIRED_HEADSET = 1;

    public  static  CopyAssets getInstance() {
        if (instance == null) {
            instance = new CopyAssets();
        }
        return instance;
    }

    public static final boolean isInstanciated() {
        return instance != null;
    }

    private CopyAssets() {}

    public synchronized void createAndStart(Context c ) {
        try {
            init(c);
            copyAssetsFromPackage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId,lFileToCopy.getName());
        }
    }

    public void copyFromPackage(int ressourceId, String target) throws IOException{
        FileOutputStream lOutputStream = mServiceContext.openFileOutput (target, 0);
        InputStream lInputStream = mR.openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while (( readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff,0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
    private void copyAssetsFromPackage() throws IOException {
        copyIfNotExist(R.raw.incoming_chat, mErrorToneFile);
        copyIfNotExist(R.raw.rootca, mRootCaFile);
        copyIfNotExist(R.raw.background, mBackgroundFile);
        copyIfNotExist(R.raw.background_calling, mBackgroundCallingFile);
        copyIfNotExist(R.raw.user, mUserFile);
    }

    public String getRingBasePath() {
        return basePath;
    }

    private void init (Context c){
        mServiceContext = c;
        mR = c.getResources();
        basePath = HjtApp.getInstance().getFilesDir().getAbsolutePath();
        mRootCaFile = basePath + "/rootca.pem";
        mErrorToneFile = basePath + "/error.wav";
        mBackgroundFile = basePath + "/background.png";
        mBackgroundCallingFile = basePath + "/background_calling.png";
        mUserFile = basePath + "/user.jpg";
        mUserCertificatePath = basePath;

        mAudioManager = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
    }

    public String processAudioRouteEvent(String event, int value){
        LOG.info("hexmeet  processAudioRouteEvent - event="+mAudioManager.getMode());
        LOG.info("hexmeet  processAudioRouteEvent - event="+event+", value="+value);

        if(event.equals(PHONE_INTERRUPTION)){
            return processAudioRouteEventPhoneInterruption(value);
        }
        else if(event.equals(HEADSET_PLUG_EVENT)){
            return processAudioRouteEventHeadsetPlug(value);
        }
        else if(event.equals(BLUETOOTH_CONNECTION_EVENT)){
            return processAudioRouteEventBluetoothConnection(value);
        }
        else if(event.equals(CONVERSATION_EVENT)){
            return processAudioRouteEventConversation(value);
        }
        else if(event.equals(CONVERSATION_AUDIOONLY_EVENT)){
            return processAudioRouteEventAudioOnlyConversation(value);
        }
        else if(event.equals(INCOMING_RING_EVENT)){
            return processAudioRouteEventIncomingRing(value);
        }
        else if(event.equals(REMOTE_RING_EVENT)){
            return processAudioRouteEventRemoteRing(value);
        }
        else if(event.equals(UI_SPEAKERLABEL_EVENT)){
            return processAudioRouteEventUISpeakerLable(value);
        }
        else if(event.equals(BLUETOOTH_CONNECTION_FAILED)){
            return processAudioRouteEventBluetootConnectionFailed();
        }
        else{
            LOG.info("hexmeet  processAudioRouteEvent - invalid event="+event);
            return null;
        }
    }

    private String processAudioRouteEventPhoneInterruption(int value){
        if(EVENT_START == value){
            // interruption start, no need to process audio route and audio mode
        }
        else if(EVENT_STOP == value){
            // interruption stop, back to previous audio route and audio mode
            if(mCurrentAudioRoute.equals(ROUTE_TO_SPEAKER)){
                routeAudioToSpeaker();
            }
            else if(mCurrentAudioRoute.equals(ROUTE_TO_RECEIVER)){
                routeAudioToReceiver();
            }
            else if(mCurrentAudioRoute.equals(ROUTE_TO_WIREDHEADSET)){
                routeAudioToReceiver();
            }
            else{ // bluetooth
                if(isBluetoothConnected()){
                    Handler postHandler = new Handler();
                    postHandler.postDelayed(new Runnable(){
                        public void run(){
                           LOG.info("hexmeet  processAudioRouteEventPhoneInterruption post run: route to Bluetooth");
                            routeAudioToBluetooth();
                        }
                    }, 500);
                }
                else{
                    LOG.info("hexmeet  processAudioRouteEventPhoneInterruption ROUTE_TO_BLUETOOTH but Bluetooth is not connected");
                    LOG.info("hexmeet  processAudioRouteEventPhoneInterruption route to speaker for workaround");
                    routeAudioToSpeaker();
                    mCurrentAudioRoute = ROUTE_TO_SPEAKER;
                }
            }
            LOG.info("hexmeet processAudioRouteEventPhoneInterruption set mode to "+mCurrentAudioMode);
            mAudioManager.setMode(mCurrentAudioMode);
        }
        else{
            LOG.info("hexmeet  processAudioRouteEventPhoneInterruption received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }

    public void routeAudioToSpeaker() {
        LOG.info("hexmeet Manager - routeAudioToSpeaker");
        routeAudioToSpeakerHelper(true);
    }

    public boolean isUsingBluetooth(){
        //return isBTPHeadSetConnected;
        return mAudioManager.isBluetoothScoOn();
    }

    public void disconnectBluetooth(){
        setBluetoothMonoState(false);
    }

    private void routeAudioToSpeakerHelper(boolean speakerOn) {
        LOG.info("hexmeet Manager - routeAudioToSpeakerHelper  "+speakerOn +",bluetooth : "+isUsingBluetooth());
        Log.w("Routing audio to : ",speakerOn ? "speaker" : "earpiece" );
        //BluetoothManager.getInstance().disableBluetoothSCO();
        if(isUsingBluetooth())
            disconnectBluetooth();

        mAudioManager.setSpeakerphoneOn(speakerOn);

    }

    private void setBluetoothMonoState(boolean isOn){
        isBTPHeadSetConnected = isOn;
        LOG.info("setBluetoothMonoState "+isOn+"  sdk int :"+android.os.Build.VERSION.SDK_INT);
        if (isOn) {
            mAudioManager.setMode(AudioManager.MODE_NORMAL); // After API level >= 11
            mAudioManager.startBluetoothSco();
            mAudioManager.setBluetoothScoOn(true);
            mAudioManager.setSpeakerphoneOn(false);
        }
        else {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            mAudioManager.setSpeakerphoneOn(true);
        }
        LOG.info("hexmeet isSetBluetoothScoOn : "+mAudioManager.isBluetoothScoOn());
    }

    public void routeAudioToReceiver() {
        LOG.info("hexmeet Manager - routeAudioToReceiver");
        routeAudioToSpeakerHelper(false);
    }

    public boolean isBluetoothConnected(){
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT < 14 || mAdapter == null || !mAdapter.isEnabled()) {
            return false;
        }

        int connectState = mAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        LOG.info("isBluetoothConnected - connectState"+connectState);
        return (connectState == BluetoothProfile.STATE_CONNECTED);
    }

    public void routeAudioToBluetooth(){
        LOG.info("hexmeet  Manager - routeAudioToBluetooth");
        setBluetoothMonoState(true);
    }

    private String processAudioRouteEventHeadsetPlug(int value){
        if(EVENT_START == value){ // plug in
            LOG.info("hexmeet processAudioRouteEventHeadsetPlug(plug in) route audio to wiredHeadset");
            routeAudioToReceiver();
            mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
            mlastHeadset = WIRED_HEADSET;
        }
        else if(EVENT_STOP == value){ // plug out
            if(mCurrentAudioRoute.equals(ROUTE_TO_WIREDHEADSET)){ // now is using this device
                if(isBluetoothConnected()){
                    LOG.info("hexmeet processAudioRouteEventHeadsetPlug(plug out) route audio to bluetooth");
                    routeAudioToBluetooth();
                    mCurrentAudioRoute = ROUTE_TO_BLUETOOTH;
                }
                else{
                    LOG.info("hexmeet processAudioRouteEventHeadsetPlug(plug out) route audio to speaker");
                    routeAudioToSpeaker();
                    mCurrentAudioRoute = ROUTE_TO_SPEAKER;
                }
            }
            else{
                // now is not using this device, maybe bluetooth or speaker, so no need to process
                LOG.info("hexmeet processAudioRouteEventHeadsetPlug(plug out) no audio route needed");
            }
        }
        else{
            LOG.info("hexmeet processAudioRouteEventHeadsetPlug received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }

    private CountDownTimer mCountDownTimer = new CountDownTimer(10000, 1000){
        public void onTick(long millisUntilFinished){
            LOG.info("hexmeet  CountDownTimer::onTick seconds remaining: "+millisUntilFinished/1000);
            try{
                if (isBluetoothConnected()) {
                    LOG.info("hexmeet  CountDownTimer::onTick turn on sco");
                    routeAudioToBluetooth();
                    cancel();
                    mCountDownTimerStarted = false;
                }
                else{
                    LOG.info("hexmeet CountDownTimer::onTick isBTHeadsetConnected() return false");
                }
            }
            catch (Exception e) {
                LOG.info("hexmeet CountDownTimer::onTick exception", e);
            }
        }

        public void onFinish()
        {
            LOG.info("hexmeet CountDownTimer::onFinish fail to open sco");
            mCountDownTimerStarted = false;
            // to route to other audio device, but no chance to change the speaker lable on the UI.
            processAudioRouteEvent(BLUETOOTH_CONNECTION_FAILED, 0);
        }
    };



    private String processAudioRouteEventBluetoothConnection(int value){
        if(EVENT_START == value){ // bluetooth connected
            LOG.info("hexmeet processAudioRouteEventBluetoothConnection(plug in) try route audio to bluetooth");
            if(isBluetoothConnected()){
                routeAudioToBluetooth();
            }
            else{
                // start count down timer to process
                LOG.info("hexmeet processAudioRouteEventBluetoothConnection(plug in) start count down timer");
                mCountDownTimerStarted = true;
                mCountDownTimer.start();
            }
            mCurrentAudioRoute = ROUTE_TO_BLUETOOTH;
            mlastHeadset = BLUETOOTH_HEADSET;
        }
        else if(EVENT_STOP == value){ // bluetooth disconnected
            if(mCurrentAudioRoute.equals(ROUTE_TO_BLUETOOTH)){ // now is using this device
                if(mAudioManager.isWiredHeadsetOn()){
                    LOG.info("hexmeet processAudioRouteEventBluetoothConnection(plug out) route audio to wiredHeadset");
                    routeAudioToReceiver();
                    mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
                }
                else{
                    LOG.info("hexmeet processAudioRouteEventBluetoothConnection(plug out) route audio to speaker");
                    routeAudioToSpeaker();
                    mCurrentAudioRoute = ROUTE_TO_SPEAKER;
                }
            }
            else{
                // now is not using this device, maybe wiredHeadset or speaker, no need to process
                LOG.info("hexmeet processAudioRouteEventBluetoothConnection(plug out) no audio route needed");
            }
        }
        else{
            LOG.info("hexmeet processAudioRouteEventBluetoothConnection received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }

    private String processAudioRouteEventConversation(int value){
        if(EVENT_START == value){ // conversation start
            LOG.info("hexmeet processAudioRouteEventConversation(start) set audio mode to MODE_IN_COMMUNICATION");
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                mAudioManager.setMode(AudioManager.MODE_NORMAL); // After API level >= 11
            }
            else {
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
            }
            mCurrentAudioMode = mAudioManager.getMode();
            LOG.info("hexmeet isBluetoothScoOn() : "+mAudioManager.isBluetoothScoOn()+",isBluetoothA2dpOn(): "+mAudioManager.isBluetoothA2dpOn());
            if(mAudioManager.isBluetoothScoOn() || mAudioManager.isBluetoothA2dpOn()){
                LOG.info("hexmeet processAudioRouteEventConversation(start) current audio route is bluetooth");
                mCurrentAudioRoute = ROUTE_TO_BLUETOOTH;
                mlastHeadset = BLUETOOTH_HEADSET;
                setBluetoothMonoState(true);
            }
            else if(mAudioManager.isWiredHeadsetOn()){
                LOG.info("hexmeet processAudioRouteEventConversation(start) current audio route is wiredHeadset");
                routeAudioToReceiver();
                mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
                mlastHeadset = WIRED_HEADSET;
            }
            else{
                // no headset connected and current route is not speaker, route to speaker
                LOG.info("hexmeet processAudioRouteEventConversation(start) route audio to receiver");
                routeAudioToSpeaker();
                mCurrentAudioRoute = ROUTE_TO_SPEAKER;
            }

        }
        else if(EVENT_STOP == value){ // conversation stop
            LOG.info("hexmeet processAudioRouteEventConversation(stop) set audio mode to MODE_NORMAL");
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mCurrentAudioMode = mAudioManager.getMode();
            // need to do special audio route?
            if(isUsingBluetooth()){
                if(mAudioManager.isWiredHeadsetOn()){
                    LOG.info("hexmeet processAudioRouteEventConversation(stop) route audio to wiredHeadset");
                    routeAudioToReceiver();
                    mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
                }
                else{
                    LOG.info("hexmeet processAudioRouteEventConversation(stop) route audio to speaker");
                    routeAudioToSpeaker();
                    mCurrentAudioRoute = ROUTE_TO_SPEAKER;
                }
            }
        }
        else{
            LOG.info("hexmeet processAudioRouteEventConversation received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }

    private String processAudioRouteEventAudioOnlyConversation(int value){
        if(EVENT_START == value){ // conversation start
            LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(start) set audio mode to MODE_IN_COMMUNICATION");
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                //mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION); // After API level >= 11
                mAudioManager.setMode(AudioManager.MODE_NORMAL); // After API level >= 11
            }
            else {
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
            }
            mCurrentAudioMode = mAudioManager.getMode();

            if(mAudioManager.isBluetoothScoOn() || mAudioManager.isBluetoothA2dpOn()){
                LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(start) current audio route is bluetooth");
                mCurrentAudioRoute = ROUTE_TO_BLUETOOTH;
                mlastHeadset = BLUETOOTH_HEADSET;
            }
            else if(mAudioManager.isWiredHeadsetOn()){
                LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(start) current audio route is wiredHeadset");
                routeAudioToReceiver();
                mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
                mlastHeadset = WIRED_HEADSET;
            }
            else{
                // no headset connected and current route is not speaker, route to speaker
                LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(start) route audio to receiver");
                routeAudioToReceiver();
                mCurrentAudioRoute = ROUTE_TO_RECEIVER;
            }
        }
        else if(EVENT_STOP == value){ // conversation stop
            LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(stop) set audio mode to MODE_NORMAL");
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mCurrentAudioMode = mAudioManager.getMode();
            // need to do special audio route?
            if(isUsingBluetooth()){
                if(mAudioManager.isWiredHeadsetOn()){
                    LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(stop) route audio to wiredHeadset");
                    routeAudioToReceiver();
                    mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
                }
                else{
                    LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation(stop) route audio to speaker");
                    routeAudioToSpeaker();
                    mCurrentAudioRoute = ROUTE_TO_SPEAKER;
                }
            }
        }
        else{
            LOG.info("hexmeet processAudioRouteEventAudioOnlyConversation received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }

    private String processAudioRouteEventIncomingRing(int value){
        if(EVENT_START == value){ // ring start
            LOG.info("hexmeet processAudioRouteEventIncomingRing set audio mode to MODE_RINGTONE, current audio route="+mCurrentAudioRoute);
            mAudioManager.setMode(AudioManager.MODE_RINGTONE);
            mCurrentAudioMode = mAudioManager.getMode();
        }
        else if(EVENT_STOP == value){ // ring stop
            LOG.info("hexmeet processAudioRouteEventIncomingRing set audio mode to MODE_NORMAL");
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mCurrentAudioMode = mAudioManager.getMode();
            // TODO
            // test if need to do audio route when there is headset,
            // since MODE_RINGTONE may route audio both to speaker and headset,
            // need test what will happen when the ring finished.
        }
        else{
            LOG.info("hexmeet processAudioRouteEventIncomingRing received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }
    private String processAudioRouteEventRemoteRing(int value){
        // maybe no need special process for remote ring event?
        // have a test!
        if(EVENT_START == value){ // ring start

        }
        else if(EVENT_STOP == value){ // ring stop

        }
        else{
            LOG.info("hexmeet processAudioRouteEventRemoteRing received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }
    private String processAudioRouteEventUISpeakerLable(int value){
        if(EVENT_START == value){ // speaker on
            LOG.info("hexmeet processAudioRouteEventUISpeakerLable(speaker on) route audio to speaker");
            routeAudioToSpeaker();
            mCurrentAudioRoute = ROUTE_TO_SPEAKER;
        }
        else if(EVENT_STOP == value){ // speaker off
            if(isBluetoothConnected() && mAudioManager.isWiredHeadsetOn()){
                // both wired headset and bluetooth, route to the latest plug one
                if(mlastHeadset == BLUETOOTH_HEADSET){
                    LOG.info("hexmeet processAudioRouteEventUISpeakerLable(speaker off) route audio to bluetooth 0");
                    routeAudioToBluetooth();
                    mCurrentAudioRoute = ROUTE_TO_BLUETOOTH;
                }
                else{
                    LOG.info("hexmeet processAudioRouteEventUISpeakerLable(speaker off) route audio to wiredHeadset 0");
                    routeAudioToReceiver();
                    mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
                }
            }
            else if(isBluetoothConnected()){
                LOG.info("hexmeet processAudioRouteEventUISpeakerLable(speaker off) route audio to bluetooth 1");
                routeAudioToBluetooth();
                mCurrentAudioRoute = ROUTE_TO_BLUETOOTH;
            }
            else if(mAudioManager.isWiredHeadsetOn()){
                LOG.info("hexmeet processAudioRouteEventUISpeakerLable(speaker off) route audio to wiredHeadset 1");
                routeAudioToReceiver();
                mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
            }
            else{
                LOG.info("hexmeet processAudioRouteEventUISpeakerLable(speaker off) route audio to receiver");
                routeAudioToReceiver();
                mCurrentAudioRoute = ROUTE_TO_RECEIVER;
            }
        }
        else{
            LOG.info("hexmeet processAudioRouteEventUISpeakerLable received invalid value="+value);
        }
        return mCurrentAudioRoute;
    }

    private String processAudioRouteEventBluetootConnectionFailed(){
        if(mAudioManager.isWiredHeadsetOn()){
            LOG.info("hexmeet processAudioRouteEventBluetootConnectionFailed route audio to wiredHeadset");
            routeAudioToReceiver();
            mCurrentAudioRoute = ROUTE_TO_WIREDHEADSET;
        }
        else{
            LOG.info("hexmeet processAudioRouteEventBluetootConnectionFailed route audio to speaker");
            routeAudioToSpeaker();
            mCurrentAudioRoute = ROUTE_TO_SPEAKER;
        }
        return mCurrentAudioRoute;
    }

}
