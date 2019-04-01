package com.hexmeet.hjt.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.utils.ScreenUtil;

public class ClearEditText extends EditText implements  
        OnFocusChangeListener, TextWatcher { 
    private Drawable mClearDrawable; 
    private boolean hasFoucs;
 
    public ClearEditText(Context context) { 
        this(context, null); 
    } 
 
    public ClearEditText(Context context, AttributeSet attrs) { 
        this(context, attrs, android.R.attr.editTextStyle); 
    } 
    
    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
   private void init()
   {
      mClearDrawable = getCompoundDrawables()[2];
      if (mClearDrawable == null)
      {
         if (isInEditMode())
         {
            return;
         }
         mClearDrawable = getResources().getDrawable(R.drawable.icon_del);
      }

      int width = ScreenUtil.dp_to_px(15);
      mClearDrawable.setBounds(0, 0, width, width);
      setClearIconVisible(false);
      setOnFocusChangeListener(this);
      addTextChangedListener(this);
   }
 
    @Override 
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {

                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }
 
    @Override 
    public void onFocusChange(View v, boolean hasFocus) { 
        this.hasFoucs = hasFocus;
        if (hasFocus) { 
            setClearIconVisible(getText().length() > 0); 
        } else { 
            setClearIconVisible(false); 
        } 
    } 
 
    protected void setClearIconVisible(boolean visible) { 
        Drawable right = visible ? mClearDrawable : null; 
        setCompoundDrawables(getCompoundDrawables()[0], 
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]); 
    } 
    
    @Override 
    public void onTextChanged(CharSequence s, int start, int count, 
            int after) { 
                if(hasFoucs){
                    setClearIconVisible(s.length() > 0);
                }
    } 
 
    @Override 
    public void beforeTextChanged(CharSequence s, int start, int count, 
            int after) { 
         
    } 
 
    @Override 
    public void afterTextChanged(Editable s) { 
         
    } 
}
