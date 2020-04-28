package com.hexmeet.hjt.sdk;


import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.view.Display;
import android.view.SurfaceView;

import com.hexmeet.hjt.model.LoginParams;

import java.io.File;
import java.util.List;

import ev.common.EVEngine;

public interface  SdkManager {

    void initSDK();

    void dropCall();

    void release();

    void logout();

    void makeCall(MakeCallParam param);

    void p2pMakeCall(MakeCallParam param);

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

    String getObtainLogPath();

    void getUserInfo();

    void zoomVideoByStreamType(EVEngine.StreamType video,float factor,float cx,float cy);

    void networkQuality();

    void onPhoneStateChange(boolean onChange);

    void onEnableSpeaker(boolean isSpeaker);

    boolean isCalling();

    void isHardDecoding(boolean hardDecoding);

    void isFrontCamera();

    void setVideoActive(boolean mode);

    boolean micEnabled();

    void refuseP2PMeeting(String number);

    void setConfDisplayName(String displayName);

    String getDisplayName();

    String getIMAddress();

    String getIMGroupId();

    EVEngine.ContactInfo getIMContactInfo(String userId);

    void setScreenShare(Context context, MediaProjection smediaProjection, Display display, Handler mhandler);

    void stopScreenShare();

    void setScreenDirection(boolean direction);

    boolean  isStatsEncrypted();

    void uploadFeedbackFiles(List<String> path, String contact, String description);

    boolean isMeetingHost();

    void onTerminateMeeting();
}
