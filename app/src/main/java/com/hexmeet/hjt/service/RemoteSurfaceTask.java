package com.hexmeet.hjt.service;

import android.view.SurfaceView;

public abstract class RemoteSurfaceTask implements Runnable {
    private SurfaceView[] view;

    public RemoteSurfaceTask(SurfaceView[] view) {
        this.view = view;
    }

    @Override
    public void run() {
        if(view != null) {
            injectSurface(view);
        }
    }

    protected abstract void injectSurface(SurfaceView[] surfaceView);
}

