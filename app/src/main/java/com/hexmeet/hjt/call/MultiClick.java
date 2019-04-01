package com.hexmeet.hjt.call;

import android.view.View;

public abstract class MultiClick implements View.OnClickListener {
    private final static long CLICK_INTERNAL_TIME = 1000L;
    private long lastClickTime = 0L;
    private int limit = 1;
    private int count = 0;
    private boolean lock = true;

    public MultiClick(int limit) {
        this.limit = limit;
    }

    @Override
    public void onClick(View v) {
        if(!lock) {
            onFinalClick();
            return;
        }
        long curTime = System.currentTimeMillis();
        if(lastClickTime < 10 ||  curTime - lastClickTime < CLICK_INTERNAL_TIME) {
            if(count >= limit) {
                reset();
                lock = false;
                onFinalClick();
            } else {
                count ++;
                lastClickTime = curTime;
            }
        } else {
            reset();
        }
    }

    private void reset() {
        count = 0;
        lastClickTime = 0L;
    }

    public abstract void onFinalClick();
}
