package com.hexmeet.hjt.widget;

import android.support.v7.widget.GridLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class NumberKeyboard {
    private TextView input;
    private GridLayout keyboard;
    private NumberKeyboardListener listener;

    public interface NumberKeyboardListener{
        void onKeyClick();
    }

    public NumberKeyboard(TextView inputView, View keyboard, NumberKeyboardListener listener) {
        this.listener = listener;
        input = inputView;
        input.addTextChangedListener(watcher);
        this.keyboard = (GridLayout) keyboard;
        init();
    }

    private void init() {
        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            child.setOnClickListener(key_click);
            if(child instanceof ImageButton) {
                child.setOnLongClickListener(delete_long_click);
            }
        }
    }

    private View.OnClickListener key_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String cur = getNumber();
            if(v instanceof ImageButton) {
                if(cur.length() > 0) {
                    input.setText(cur.substring(0, cur.length() - 1));
                }
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(cur).append(((TextView)v).getText().toString());
                input.setText(sb.toString());
            }
            listener.onKeyClick();
        }
    };

    private View.OnLongClickListener delete_long_click = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            input.setText("");
            return true;
        }
    };

    public String getNumber() {
        return input.getText().toString().trim();
    }

    public void setNumberFromRecent(String number) {
        input.setText(number);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                // No entered text so will show hint
                input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            } else {
                input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
