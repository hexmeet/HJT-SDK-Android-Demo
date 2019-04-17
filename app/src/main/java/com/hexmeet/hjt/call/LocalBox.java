package com.hexmeet.hjt.call;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hexmeet.hjt.HjtApp;

import org.apache.log4j.Logger;

import ev.common.EVEngine;
import ev.common.EVFactory;


public class LocalBox {
    private Logger LOG = Logger.getLogger(this.getClass());
    private SurfaceView surfaceView;
    private boolean ready = false;

    public LocalBox(Context context) {
        surfaceView = EVFactory.createWindow(context, EVEngine.WindowType.LocalVideoWindow);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LOG.info("mLocalView display surface is being changed. format: " + format + ", width: "
                        + width + ", height: " + height + ", surface: " + holder.getSurface());
                ready = true;
                HjtApp.getInstance().getAppService().setLocalViewToSdk(surfaceView);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LOG.info("mLocalView display surface created");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                HjtApp.getInstance().getAppService().setLocalViewToSdk(null);
                LOG.info("mLocalView display surface destroyed");
            }
        });

        applyZoom();
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public boolean isReady() {
        return ready;
    }

    public void setVisible(int visibility) {
        getSurfaceView().setVisibility(visibility);
    }

    private void applyZoom() {
        HjtApp.getInstance().getAppService().zoomVideoByStreamType(EVEngine.StreamType.Video,1.f, 0.5f, 0.5f);
    }
}
