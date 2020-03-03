package com.hexmeet.hjt.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.andreabaccega.widget.FormEditText;

import androidx.appcompat.widget.AppCompatEditText;

public class DrawableEditText extends FormEditText {

    // 构造方法
    public DrawableEditText(Context context) {
        super(context);
    }

    public DrawableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 触摸事件
    // 判断DrawableLeft/DrawableRight是否被点击
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 触摸状态
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // 监听DrawableRight
            if (onDrawableRightListener != null) {
                Drawable drawableRight = getCompoundDrawables()[2];
                // 当按下的位置 > 在EditText的到右边间距-图标的宽度-Padding
                if (drawableRight != null && event.getRawX() >= (getRight() - getTotalPaddingRight() - drawableRight.getBounds().width())) {
                    // 执行DrawableRight点击事件
                    onDrawableRightListener.onDrawableRightClick();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // 定义一个DrawableRight点击事件接口
    public interface OnDrawableRightListener {
        void onDrawableRightClick();
    }

    private OnDrawableRightListener onDrawableRightListener;

    public void setOnDrawableRightListener(OnDrawableRightListener onDrawableRightListener) {
        this.onDrawableRightListener = onDrawableRightListener;
    }

}
