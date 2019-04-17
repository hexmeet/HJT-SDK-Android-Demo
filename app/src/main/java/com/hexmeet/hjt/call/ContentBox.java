package com.hexmeet.hjt.call;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.utils.ResourceUtils;

import org.apache.log4j.Logger;

import ev.common.EVEngine;
import ev.common.EVFactory;

public class ContentBox {
    private Logger LOG = Logger.getLogger(this.getClass());
    private SurfaceView surfaceView;
    private float mZoomFactor = 1.f;
    private float mZoomCenterX, mZoomCenterY;
    private boolean isSurfaceReady = false;

    public ContentBox(Context context) {
        surfaceView = EVFactory.createWindow(context, EVEngine.WindowType.RemoteContentWindow);;

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LOG.info("mContentView display surface is being changed. format: " + format + ", width: "
                        + width + ", height: " + height + ", surface: " + holder.getSurface());
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LOG.info("mContentView display surface created");
                isSurfaceReady = true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                HjtApp.getInstance().getAppService().setContentViewToSdk(null);
                LOG.info("mContentView display surface destroyed");
            }
        });
        resetZoom();
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setVisible(int visibility) {
        getSurfaceView().setVisibility(visibility);
    }

    public void resetZoom() {
        mZoomFactor = 1.f;
        mZoomCenterX = mZoomCenterY = 0.5f;
        applyZoom();
    }

    private boolean applyZoom() {
        HjtApp.getInstance().getAppService().zoomVideoByStreamType(EVEngine.StreamType.Content,mZoomFactor, mZoomCenterX, mZoomCenterY);
        return true;
    }

    public boolean isSurfaceReady() {
        return isSurfaceReady;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mZoomFactor > 1) {
            // Video is zoomed, slide is used to change center of zoom
            if (distanceX > 0 && mZoomCenterX < 1) {
                mZoomCenterX += 0.01;
            } else if (distanceX < 0 && mZoomCenterX > 0) {
                mZoomCenterX -= 0.01;
            }
            if (distanceY < 0 && mZoomCenterY < 1) {
                mZoomCenterY += 0.01;
            } else if (distanceY > 0 && mZoomCenterY > 0) {
                mZoomCenterY -= 0.01;
            }

            if (mZoomCenterX > 1)
                mZoomCenterX = 1;
            if (mZoomCenterX < 0)
                mZoomCenterX = 0;
            if (mZoomCenterY > 1)
                mZoomCenterY = 1;
            if (mZoomCenterY < 0)
                mZoomCenterY = 0;

            applyZoom();
        }
        return false;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        mZoomFactor *= detector.getScaleFactor();
        // Don't let the object get too small or too large.
        // Zoom to make the video fill the screen vertically
        float portraitZoomFactor = ((float) ResourceUtils.screenHeight)
                / (float) ((9 * ResourceUtils.screenWidth) / 16);
        // Zoom to make the video fill the screen horizontally
        float landscapeZoomFactor = ((float) ResourceUtils.screenWidth)
                / (float) ((9 * ResourceUtils.screenHeight) / 16);
        mZoomFactor = Math.max(
                0.1f,
                Math.min(mZoomFactor,
                        Math.max(portraitZoomFactor, landscapeZoomFactor)));

        return applyZoom();
    }
}
