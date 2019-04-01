package com.hexmeet.hjt.sdk;


import android.view.SurfaceView;

import com.hexmeet.hjt.model.LoginParams;

import java.io.File;

import ev.common.EVEngine;

public interface  SdkManager {

    void initSDK();

    void dropCall();

    void release();

    void logout();

    void makeCall(MakeCallParam param);

    void answerCall(MakeCallParam param);

    void enableVideo(boolean enable);

    void handUp();

    void setMicMute(boolean mute);

    void reLoadCamera();

    void setSvcLayoutMode(int svcLayoutMode);

    void switchCamera();

    void setLocalSurface(SurfaceView view);

    void setRemoteSurface(Object[] view);

    void setPreSurface(SurfaceView view);

    void setContentSurface(SurfaceView view);

    ChannelStatList getMediaStatics();

    void uploadAvatar(File file);

    void downloadAvatar();

    void updateVideoUserImage(File imageFile);

    void rename(String name);

    void updatePassword(String oldPassword,String password);

    void login(LoginParams params, boolean https, String port);

    void anonymousMakeCall();

    void setDeviceRotation(int deviceRotation);

    void getObtainLogPath();

    void getUserInfo();

    void zoomVideoByStreamType(EVEngine.StreamType video,float factor,float cx,float cy);

    void networkQuality();

    void onPhoneStateChange(boolean onChange);

    void onEnableSpeaker(boolean isSpeaker);

    boolean isCalling();

}
