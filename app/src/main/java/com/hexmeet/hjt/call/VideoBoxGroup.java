package com.hexmeet.hjt.call;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.sdk.MessageOverlayInfo;
import com.hexmeet.hjt.sdk.SvcLayoutInfo;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.widget.MarqueeView;

import org.apache.log4j.Logger;

public class VideoBoxGroup {
    private Logger LOG = Logger.getLogger(this.getClass());
    private RelativeLayout rootView;

    private RemoteBox remoteBox;
    private LocalBox localBox;
    private ContentBox contentBox;

    private RelativeLayout.LayoutParams nullPar, fullScreenLayoutPara;
    private RelativeLayout.LayoutParams localLayoutParaForSvc;
    private RelativeLayout.LayoutParams messagePara;
    private boolean remoteCellReady = false;
    private MarqueeView message;
    private boolean isContentSurfaceShowing = false;

    public VideoBoxGroup(RelativeLayout rootView) {
        this.rootView = rootView;
        initLayoutParams();
    }

    public void buildDefaultCells(boolean localMute) {
        localBox = new LocalBox(rootView.getContext());
        remoteBox = new RemoteBox(rootView.getContext());
        remoteBox.setSvcListener(new RemoteBox.SvcSurfaceListener() {
            @Override
            public void onAllSurfaceReady() {
                remoteCellReady = true;
                HjtApp.getInstance().getAppService().setRemoteViewToSdk(remoteBox.getAllSurfaces());

            }
        });
        contentBox = new ContentBox(rootView.getContext());


        localBox.getSurfaceView().setZOrderOnTop(true);
        localBox.getSurfaceView().setZOrderMediaOverlay(true);

        rootView.addView(contentBox.getSurfaceView(), 1, nullPar);
        rootView.addView(remoteBox, 2, fullScreenLayoutPara);
        rootView.addView(localBox.getSurfaceView(), 3, SystemCache.getInstance().isUserShowLocalCamera() && !isContentSurfaceShowing ? localLayoutParaForSvc : nullPar);

        message = new MarqueeView(rootView.getContext());
        message.setMarquessRepeatedListener(new MarqueeView.MarquessRepeatedListener() {
            @Override
            public void onRepeatEnd() {
                message.setVisibility(View.GONE);
                SystemCache.getInstance().clearOverlayMessage();
            }
        });
        rootView.addView(message, 4, messagePara);
        message.setVisibility(View.GONE);

        rootView.addView(localBox.getLocalCellInfoView(rootView.getContext()), 5, localBox.getCellInfoLayoutParams());
        updateLocalMute(localMute);

    }

    public void updateLocalMute(boolean localMute){
        LOG.info("MUTE : "+localMute);
        if(localBox != null){
            localBox.updateCellMuteState(localMute);
        }
    }

    public void release() {
        if(remoteBox!=null){
            remoteBox.release();
            rootView.removeAllViews();
        }
    }

    private void initLayoutParams() {
            /*
         * nullLayoutPara之所以设置为1个像素而不是0个像素的原因：
         * 发现如果设置为0个像素之后，对应的surfaceview在activity finish以后不会自动destroy，
         * 即使手动将其从父窗口remove掉，surfaceDestroyed回调函数也不会被调用，所以将其设置为1个像素
         */
        nullPar = new RelativeLayout.LayoutParams(1, 1);
        nullPar.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        nullPar.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        fullScreenLayoutPara = new RelativeLayout.LayoutParams(ResourceUtils.screenWidth, ResourceUtils.screenHeight);
        fullScreenLayoutPara.addRule(RelativeLayout.CENTER_IN_PARENT);

        localLayoutParaForSvc = new RelativeLayout.LayoutParams(ResourceUtils.screenWidth/5, ResourceUtils.screenHeight/5);
        localLayoutParaForSvc.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        localLayoutParaForSvc.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        localLayoutParaForSvc.bottomMargin = ResourceUtils.screenHeight/35;
        localLayoutParaForSvc.rightMargin = (ResourceUtils.horizontalMargin + ResourceUtils.screenHeight/35);

        messagePara = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    public boolean updateSvcLayout(SvcLayoutInfo info) {
        LOG.info("VideoBoxGrp: updateSvcLayout remoteCellReady? "+remoteCellReady );
        if(remoteBox != null && remoteCellReady) {
            remoteBox.updateLayout(info);
            return true;
        }
        return false;
    }

    public boolean updateSvcSpeaker(int index, String siteName) {
        if(remoteCellReady) {
            remoteBox.updateSpeaker(index, siteName);
            return true;
        }
        return false;
    }
    public boolean updateMicMute(String  participants) {
        if(remoteCellReady) {
            remoteBox.updateMicMute(participants);
            return true;
        }
        return false;
    }

    public boolean isContentReady() {
        return contentBox != null && contentBox.isSurfaceReady();
    }

    public void updateContent(boolean showContent) {
        isContentSurfaceShowing = showContent;
        contentBox.resetZoom();
        if(showContent) {
            remoteBox.hideAll();
            remoteBox.setShowContent(true);
            remoteBox.setLayoutParams(nullPar);
            showLocalCamera(false);
            contentBox.getSurfaceView().setLayoutParams(fullScreenLayoutPara);
            pauseMessageOverlay();
        } else {
            remoteBox.setShowContent(false);
            contentBox.getSurfaceView().setLayoutParams(nullPar);
            showLocalCamera(SystemCache.getInstance().isUserShowLocalCamera());
            remoteBox.setLayoutParams(fullScreenLayoutPara);
            resumeMessageOverlay();
        }
    }

    public SurfaceView getContentSurface() {
        return contentBox != null ? contentBox.getSurfaceView() : null;
    }

    public void onLayoutModeChanged() {
        if(remoteCellReady) {
            remoteBox.refreshLayout();
        }
    }

    public void showLocalCamera(boolean show) {
        if(localBox != null && localBox.getSurfaceView() != null) {
            if(show) {
                if(isContentSurfaceShowing)  {
                    localBox.getSurfaceView().setLayoutParams(nullPar);
                } else {
                    localBox.getSurfaceView().setLayoutParams(localLayoutParaForSvc);
                }
            } else {
                localBox.getSurfaceView().setLayoutParams(nullPar);
            }
        }
    }

    public void updateMessageView(int margin) {
        messagePara.topMargin = Math.max(margin, 40);
        message.setLayoutParams(messagePara);
    }

    public void showMessage(boolean show) {
        if(message == null) {
            return;
        }
        if(show && !isContentSurfaceShowing) {
            message.setVisibility(View.VISIBLE);
        } else {
            message.stop();
            message.setVisibility(View.GONE);
        }
    }

    private void pauseMessageOverlay() {
        if(message == null) {
            return;
        }

        message.setVisibility(View.GONE);
    }

    private void resumeMessageOverlay() {
        if(message == null) {
            return;
        }
        if(message.isRunning()){
            message.setVisibility(View.VISIBLE);
            message.invalidate();
        }

    }

    public boolean updateMessageOverlay(MessageOverlayInfo messageOverlayInfo) {
        if(remoteCellReady && message != null) {
            LOG.info("Update SVC Message overlay -> "+messageOverlayInfo.toString());

            if(messageOverlayInfo != null) {
                applyMessage(messageOverlayInfo);
            }
            return true;
        }
        return false;
    }

    private void applyMessage(MessageOverlayInfo info) {
        if(info.isOn()) {
            showMessage(true);
            int speedMode = MarqueeView.SCROLL_SPEED_MIDDLE;
            switch (info.getDisplaySpeed()) {
                case 0:
                    speedMode = MarqueeView.SCROLL_SPEED_STATIC;
                    break;
                case 2:
                    speedMode = MarqueeView.SCROLL_SPEED_SLOW;
                    break;
                case 5:
                    speedMode = MarqueeView.SCROLL_SPEED_MIDDLE;
                    break;
                case 10:
                    speedMode = MarqueeView.SCROLL_SPEED_FAST;
                    break;
            }

            int bgColor = Color.WHITE;
            String alpha = Integer.toHexString(255 * Math.abs(100 - info.getTransparency())/100);
            if(alpha.length() < 2) {
                alpha = "0" + alpha;
            }
            try {
                if(!TextUtils.isEmpty(info.getBackgroundColor())) {
                    String colorString = info.getBackgroundColor();
                    if(colorString.startsWith("#")) {
                        colorString = "#" + alpha + colorString.substring(1, colorString.length());
                    } else {
                        colorString = "#" + alpha + colorString;
                    }
                    LOG.info("MessageOverlay: combine bg color: ["+colorString+"]");
                    bgColor = Color.parseColor(colorString);
                }
            } catch (Exception e) {
                LOG.error("MessageOverlay: combine bg color error ", e);
            }
            message.setText(info.getContent(), bgColor, getTextColor(info.getForegroundColor()), (int)(info.getFontSize() * 3), info.getDisplayRepetitions(), speedMode);
        } else {
            showMessage(false);
        }
    }

    public boolean isRemoteCellReady() {
        return remoteCellReady;
    }

    private int getTextColor(String colorStr) {
        if(TextUtils.isEmpty(colorStr)) {
            colorStr = "#000000";
        }
        return Color.parseColor(colorStr);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(isContentSurfaceShowing) {
            return contentBox.onScroll(e1, e2, distanceX, distanceY);
        }

        return false;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        if(isContentSurfaceShowing) {
            return contentBox.onScale(detector);
        }
        return false;
    }
}
