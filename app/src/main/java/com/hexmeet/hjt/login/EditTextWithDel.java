package com.hexmeet.hjt.login;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.andreabaccega.widget.FormEditText;
import com.hexmeet.hjt.R;

import org.apache.log4j.Logger;

public class EditTextWithDel extends FormEditText {
    private Logger LOG = Logger.getLogger(Login.class);
    private Drawable imgInable;
    private Context mContext;

    public EditTextWithDel(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        imgInable = mContext.getResources().getDrawable(R.drawable.btn_del_xh);
       // imgInable .setBounds(20, 100, 20, 105);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LOG.info("onTextChanged()");

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                LOG.info("beforeTextChanged()");
            }

            @Override
            public void afterTextChanged(Editable s) {
                LOG.info("afterTextChanged()");
                setDrawable();
            }
        });
        setDrawable();
    }

    // 设置删除图片
    private void setDrawable() {
        if (length() < 1)
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        else
            setCompoundDrawablesWithIntrinsicBounds(null, null, imgInable, null);
    }

    // 处理删除事件
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imgInable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getRawX();
            int eventY = (int) event.getRawY();
            LOG.info("eventX = " + eventX + "; eventY = " + eventY);
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - imgInable.getIntrinsicWidth();
            rect.bottom =rect.top+rect.height()/2+imgInable.getIntrinsicHeight()/2;
            rect.top = rect.bottom - imgInable.getIntrinsicHeight();
            if (rect.contains(eventX, eventY))
                setText("");
        }
        return super.onTouchEvent(event);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}

