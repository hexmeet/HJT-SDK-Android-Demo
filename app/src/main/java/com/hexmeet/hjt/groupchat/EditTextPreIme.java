package com.hexmeet.hjt.groupchat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditTextPreIme extends EditText {

    private OnKeysBackListener onKeyBackListener;

    public EditTextPreIme(Context context) {
        super(context);
    }

    public EditTextPreIme(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreIme(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnKeyBackListener(OnKeysBackListener onKeyBackListener) {
        this.onKeyBackListener = onKeyBackListener;

    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//            ((Activity) this.getContext()).onKeyDown(KeyEvent.KEYCODE_BACK, event);
            if (null != onKeyBackListener) {
                return onKeyBackListener.onKeyBack();
            } else {
                return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    protected interface OnKeysBackListener {
        public boolean onKeyBack();
    }
}
