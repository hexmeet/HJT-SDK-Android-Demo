package com.hexmeet.hjt.call;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.sdk.SvcLayoutInfo;
import com.hexmeet.hjt.utils.ResourceUtils;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ev.common.EVEngine;
import ev.common.EVFactory;

public class RemoteBox extends RelativeLayout {
    private Logger LOG = Logger.getLogger(this.getClass());
    private static final int INDEX_INFO_SITE_NAME = 1;
    private static final int INDEX_INFO_LOCAL_MUTE = 0;

    private SvcSurfaceListener listener;
    private SurfaceView[] surfaceViews = new SurfaceView[AppCons.MAX_RECEIVE_STREAM];
    private LinearLayout[] msgInfo = new LinearLayout[AppCons.MAX_RECEIVE_STREAM];
    private boolean[] surfaceViewValid = new boolean[AppCons.MAX_RECEIVE_STREAM];
    private View borderView;
    private LayoutParams hideParam, emptyParam;
    private LinearLayout.LayoutParams infoCellParam, cellEmptyParam;
    private int speakerIndex = -1;
    private SvcLayoutInfo svcLayoutInfo;
    private boolean speakerMode = false;
    private boolean showContent = true;
    private AtomicInteger surfaceReadyCount = new AtomicInteger(0);

    public interface SvcSurfaceListener {
        void onAllSurfaceReady();
    }

    public RemoteBox(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LOG.info("SVC Group: onSizeChanged [w:" + w + " h:" + h + "]");
        refreshLayout();
    }

    public void refreshLayout() {
        if (svcLayoutInfo != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateLayout(svcLayoutInfo);
                }
            }, 500);
        }
    }

    public void setSvcListener(SvcSurfaceListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        hideParam = new LayoutParams(1, 1);
        emptyParam = new LayoutParams(0, 0);
        cellEmptyParam = new LinearLayout.LayoutParams(0, 0);
        infoCellParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoCellParam.gravity = Gravity.CENTER_VERTICAL;
        infoCellParam.rightMargin = 5;
        infoCellParam.leftMargin = 5;

        surfaceReadyCount.set(0);
        for (int i = 0; i < AppCons.MAX_RECEIVE_STREAM; i++) {
            SurfaceView surfaceView = EVFactory.createWindow(this.getContext(), EVEngine.WindowType.RemoteVideoWindow);
            surfaceView.setId(ResourceUtils.generateViewId());
            final int pos = i;
            surfaceViewValid[i] = false;
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    LOG.info("--SVC-surfaceCreated[" + pos + "]-->" + holder.getSurface() + "<---");
                    surfaceViewValid[pos] = true;
                    if(pos == 0) {
                        surfaceReadyCount.set(0);
                    }
                    if (surfaceReadyCount.incrementAndGet() == AppCons.MAX_RECEIVE_STREAM && listener != null) {
                        listener.onAllSurfaceReady();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    LOG.info("--SVC-surfaceChanged[" + pos + "]-->" + holder.getSurface().toString() + "<--[" + width + "x" + height + "]-");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    surfaceViewValid[pos] = false;
                    Object[] surfaces = getAllSurfaces();
                    HjtApp.getInstance().getAppService().setRemoteViewToSdk(surfaces);
                    LOG.info("--SVC-surfaceDestroyed[" + pos + "]-->" + holder.getSurface().toString());
                }
            });

            LOG.info("--SVC-surface Added[" + pos + "]--");
            addView(surfaceView, hideParam);
            surfaceViews[i] = surfaceView;
        }

        for (int i = 0; i < AppCons.MAX_RECEIVE_STREAM; i++) {
            LinearLayout infoContainer = new LinearLayout(context);
            infoContainer.setBackgroundResource(R.drawable.bg_svc_suit_text);
            infoContainer.setGravity(Gravity.CENTER_VERTICAL);

            ImageView localMuteIcon = new ImageView(context);
            localMuteIcon.setImageResource(R.drawable.icon_local_mute_large);
            infoContainer.addView(localMuteIcon, cellEmptyParam);

            TextView textView = new TextView(context);
            textView.setTextColor(Color.WHITE);
            textView.setPadding(5, 2, 5, 2);
            textView.setMaxLines(1);

            infoContainer.addView(textView, infoCellParam);

            addView(infoContainer, emptyParam);
            msgInfo[i] = infoContainer;
        }

        borderView = new View(context);
        borderView.setBackgroundResource(R.drawable.bg_speaker_border);
        addView(borderView, emptyParam);
    }

    public Object[] getAllSurfaces() {
        Object[] array = new Object[AppCons.MAX_RECEIVE_STREAM];
        for (int i = 0; i < AppCons.MAX_RECEIVE_STREAM; i++) {
            if (surfaceViewValid[i] && surfaceViews[i] != null && surfaceViews[i].getHolder() != null && surfaceViews[i].getHolder().getSurface() != null) {
                array[i] = surfaceViews[i];
            } else {
                array[i] = null;
            }

        }
        return array;
    }

    public void updateLayout(SvcLayoutInfo svcLayoutInfo) {
        if (!svcLayoutInfo.checkSize(AppCons.MAX_RECEIVE_STREAM)) {
            LOG.error("surface count != sitename count");
        }
        this.svcLayoutInfo = svcLayoutInfo;

        int count = svcLayoutInfo.getSvcSuit().size();
        //speakerMode = svcLayoutInfo.getLayoutMode() != null && svcLayoutInfo.getLayoutMode().equalsIgnoreCase("Speaker");
        speakerMode = AppSettings.getInstance().isSpeakerMode();

        borderView.setLayoutParams(emptyParam);
        if (count == 0 || (SystemCache.getInstance().withContent() && showContent)) {
            speakerIndex = -1;
            hideAll();
            return;
        }

        int width = getWidth() == 0 ? ResourceUtils.screenWidth : getWidth();
        int height = getHeight() == 0 ? ResourceUtils.screenHeight : getHeight();
        int textSize = 14;
        int siteIcon = R.drawable.icon_local_mute_small;

        int indexInArray = !TextUtils.isEmpty(svcLayoutInfo.getSpeakerName()) ? svcLayoutInfo.getSvcSuit().indexOf(svcLayoutInfo.getSpeakerName()) : -1;
        speakerIndex = speakerMode ? 0 : (indexInArray == -1 ? -1 : svcLayoutInfo.getWindowIdx().get(indexInArray));

        LOG.info("Speaker? [" + speakerMode + "], speakerIndex: " + speakerIndex + " speakerName: " + svcLayoutInfo.getSpeakerName());

        if (count == 1 || speakerMode) {
            layoutSurface(svcLayoutInfo.getWindowIdx().get(0), width, height, 0, 0);
            layoutEndpointName(svcLayoutInfo.getWindowIdx().get(0), 0, textSize, siteIcon);
            hideOtherCells(svcLayoutInfo.getWindowIdx());
            return;
        }

        int cellIndex = 0;
        if (count == 2) {
            int cellWidth = (width) / 2;
            int cellHeight = (height) / 2;

            cellIndex = svcLayoutInfo.getWindowIdx().get(0);
            layoutSurface(cellIndex, cellWidth, cellHeight, 0, cellHeight / 2);
            layoutEndpointName(cellIndex, 0, textSize, siteIcon);

            cellIndex = svcLayoutInfo.getWindowIdx().get(1);
            layoutSurface(cellIndex, cellWidth, cellHeight, cellWidth, cellHeight / 2);
            layoutEndpointName(cellIndex, 1, textSize, siteIcon);

            hideOtherCells(svcLayoutInfo.getWindowIdx());
            return;
        }

        int cellWidth = (width) / 2;
        int cellHeight = (height) / 2;

        cellIndex = svcLayoutInfo.getWindowIdx().get(0);
        layoutSurface(cellIndex, cellWidth, cellHeight, 0, 0);
        layoutEndpointName(cellIndex, 0, textSize, siteIcon);

        cellIndex = svcLayoutInfo.getWindowIdx().get(1);
        layoutSurface(cellIndex, cellWidth, cellHeight, cellWidth, 0);
        layoutEndpointName(cellIndex, 1, textSize, siteIcon);

        cellIndex = svcLayoutInfo.getWindowIdx().get(2);
        layoutSurface(cellIndex, cellWidth, cellHeight, 0, cellHeight);
        layoutEndpointName(cellIndex, 2, textSize, siteIcon);

        if (count == 4) {
            cellIndex = svcLayoutInfo.getWindowIdx().get(3);
            layoutSurface(cellIndex, cellWidth, cellHeight, cellWidth, cellHeight);
            layoutEndpointName(cellIndex, 3, textSize, siteIcon);
        }
        hideOtherCells(svcLayoutInfo.getWindowIdx());
    }

    private void hideOtherCells(List<Integer> indexList) {
        for (int i = 0; i < AppCons.MAX_RECEIVE_STREAM; i++) {
            if (!indexList.contains(i)) {
                surfaceViews[i].setLayoutParams(hideParam);
                msgInfo[i].setLayoutParams(emptyParam);
            }
        }
    }

    private void layoutEndpointName(int cellIndex, int indexInArray, int textSize, int iconRes) {
        boolean muted = false;
        if(indexInArray < svcLayoutInfo.getSvcDeviceIds().size()) {
            String deviceId = svcLayoutInfo.getSvcDeviceIds().get(indexInArray);
            msgInfo[cellIndex].setTag(deviceId);
            muted = SystemCache.getInstance().isRemoteDeviceMicMuted(deviceId);
        }

        if (TextUtils.isEmpty(svcLayoutInfo.getSvcSuit().get(indexInArray))) {
            msgInfo[cellIndex].setLayoutParams(emptyParam);
        } else {
            ((ImageView) msgInfo[cellIndex].getChildAt(INDEX_INFO_LOCAL_MUTE)).setImageResource(iconRes);
            msgInfo[cellIndex].getChildAt(INDEX_INFO_LOCAL_MUTE).setLayoutParams(muted ? infoCellParam : cellEmptyParam);

            LayoutParams msgLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            msgLp.addRule(RelativeLayout.ALIGN_BOTTOM, surfaceViews[cellIndex].getId());
            msgLp.addRule(RelativeLayout.ALIGN_LEFT, surfaceViews[cellIndex].getId());
            msgInfo[cellIndex].setLayoutParams(msgLp);
            ((TextView) msgInfo[cellIndex].getChildAt(INDEX_INFO_SITE_NAME)).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            ((TextView) msgInfo[cellIndex].getChildAt(INDEX_INFO_SITE_NAME)).setText(svcLayoutInfo.getSvcSuit().get(indexInArray));
        }
    }

    private void layoutSurface(int index, int width, int height, int marginLeft, int marginTop) {
        LayoutParams cellPara = new LayoutParams(width, height);
        cellPara.topMargin = marginTop;
        cellPara.leftMargin = marginLeft;
        surfaceViews[index].setLayoutParams(cellPara);

        if (index == speakerIndex) {
            LayoutParams borderParam = new LayoutParams(width, height);
            borderParam.topMargin = marginTop;
            borderParam.leftMargin = marginLeft;
            borderView.setLayoutParams(borderParam);
        }
    }

    public void hideAll() {
        for (int i = 0; i < AppCons.MAX_RECEIVE_STREAM; i++) {
            surfaceViews[i].setLayoutParams(hideParam);
            msgInfo[i].setLayoutParams(emptyParam);
        }
        borderView.setLayoutParams(emptyParam);
    }

    public void updateSpeaker(int index, String speaker) {
        int indexInArray = !TextUtils.isEmpty(speaker) && svcLayoutInfo != null ? svcLayoutInfo.getSvcSuit().indexOf(speaker) : -1;
        speakerIndex = indexInArray == -1 ? -1 : svcLayoutInfo.getWindowIdx().get(indexInArray);
        if (svcLayoutInfo != null) {
            svcLayoutInfo.setSpeakerName(speaker);
        }
        LOG.info("Update SVC speaker index: [" + speakerIndex + "]");
        if (speakerIndex == -1) {
            borderView.setLayoutParams(emptyParam);
        } else {
            if (speakerMode) {
                showSpeakerBorder(svcLayoutInfo.getWindowIdx().get(0));
            } else {
                showSpeakerBorder(speakerIndex);
            }
        }
    }

    private void showSpeakerBorder(int index) {
        LayoutParams speakerParams = (LayoutParams) surfaceViews[index].getLayoutParams();
        LayoutParams borderParam = new LayoutParams(speakerParams.width, speakerParams.height);
        borderParam.topMargin = speakerParams.topMargin;
        borderParam.leftMargin = speakerParams.leftMargin;
        borderView.setLayoutParams(borderParam);
    }

    public void updateMicMute(String participants) {
        View muteView = findViewWithTag(participants);
        if (muteView != null && muteView instanceof LinearLayout) {
            ((LinearLayout) muteView).getChildAt(INDEX_INFO_LOCAL_MUTE).setLayoutParams(SystemCache.getInstance().isRemoteDeviceMicMuted(participants) ? infoCellParam : cellEmptyParam);
        }
    }

    public void setShowContent(boolean showContent) {
        this.showContent = showContent;
    }


    public void release() {
        surfaceReadyCount.set(0);
        removeAllViews();
    }
}
