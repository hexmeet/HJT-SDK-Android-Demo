package com.hexmeet.hjt.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class BaseSafelyHandler<T> extends Handler {

	private final WeakReference<T> ref;

	public BaseSafelyHandler(T ref) {
		this.ref = new WeakReference<T>(ref);
	}

	public BaseSafelyHandler(Looper looper, T ref) {
	    super(looper);
        this.ref = new WeakReference<T>(ref);
    }
	
	@Override
	public final void handleMessage(Message msg) {
		T t = null;
		if ((t = ref.get()) != null) {
			handleMessage(t, msg);
		}
	}
	
	public abstract void handleMessage(T ref, Message msg);
}
