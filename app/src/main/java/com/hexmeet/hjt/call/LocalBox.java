package com.hexmeet.hjt.call;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.utils.ResourceUtils;

import org.apache.log4j.Logger;

import ev.common.EVEngine;
import ev.common.EVFactory;


public class LocalBox {
    private Logger LOG = Logger.getLogger(this.getClass());
    private SurfaceView surfaceView;
    private LinearLayout infoContainer;
    private boolean ready = false;

    public LocalBox(Context context) {
        surfaceView = EVFactory.createWindow(context, EVEngine.WindowType.LocalVideoWindow);
        surfaceView.setBackgroundResource(R.drawable.bg_localbox_border);
        surfaceView.setId(ResourceUtils.generateViewId());

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

    private void initCellInfo(Context context) {
        infoContainer = new LinearLayout(context);
        infoContainer.setBackgroundResource(R.drawable.bg_svc_suit_text);
        infoContainer.setGravity(Gravity.CENTER_VERTICAL);

        ImageView localMuteIcon = new ImageView(context);
        localMuteIcon.setImageResource(R.drawable.icon_local_mute_small);
        localMuteIcon.setVisibility(View.GONE);
        infoContainer.addView(localMuteIcon);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(3, 0, 3, 2);
        textView.setMaxLines(1);
        textView.setText(EVFactory.createEngine().getDisplayName());

        infoContainer.addView(textView);
    }

    public void adjustCellInfoPosition() {
        infoContainer.setLayoutParams(getCellInfoLayoutParams());
    }

    public RelativeLayout.LayoutParams getCellInfoLayoutParams() {
        RelativeLayout.LayoutParams msgLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        msgLp.addRule(RelativeLayout.ALIGN_BOTTOM ,surfaceView.getId());
        msgLp.addRule(RelativeLayout.ALIGN_LEFT , surfaceView.getId());
        return msgLp;
    }

    public void updateCellMuteState(boolean mute) {
        infoContainer.getChildAt(0).setVisibility(mute ? View.VISIBLE : View.GONE);
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public LinearLayout getLocalCellInfoView(Context context) {
        if(infoContainer == null) {
            initCellInfo(context);
        }
        return infoContainer;
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
