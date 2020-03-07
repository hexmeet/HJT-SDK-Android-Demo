package com.hexmeet.hjt.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hexmeet.hjt.R;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;

@SuppressLint("AppCompatCustomView")
public class KeyboardWindow extends EditText{
    private Logger LOG = Logger.getLogger(this.getClass());
    private View anchorView;
    private EditText editText;
    private int[] commonButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
           R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine};

    public KeyboardWindow(Context context, View anchorView, EditText editText) {
        super(context);
        KeyboardWindow(anchorView,editText);
    }


    public void  KeyboardWindow(View anchorView, EditText editText) {
        LOG.info("initView");
        this.anchorView = anchorView;
        this.editText = editText;
        editText.addTextChangedListener(watcher);
        initConfig();
        initView();
    }


    private void initConfig() {
        forbidDefaultSoftKeyboard();
    }

    /**
     * 禁止系统默认的软键盘弹出
     */
    private void forbidDefaultSoftKeyboard() {
        if (editText == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT > 10) {//4.0以上，使用反射的方式禁止系统自带的软键盘弹出
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void initView() {
        initKeyboardView(anchorView);
    }

    private void initKeyboardView(View view) {
        //①给数字键设置点击监听
        for (int i = 0; i < commonButtonIds.length; i++) {
            final Button button = view.findViewById(commonButtonIds[i]);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curSelection = editText.getSelectionStart();
                    int length = editText.getText().toString().length();
                    if (curSelection < length) {
                        String content = editText.getText().toString();
                        editText.setText(content.substring(0, curSelection) + button.getText() + content.subSequence(curSelection, length));
                        editText.setSelection(curSelection + 1);
                    } else {
                        editText.setText(editText.getText().toString() + button.getText());
                        editText.setSelection(editText.getText().toString().length());
                    }
                }
            });
        }

        //②给*按键设置点击监听
        view.findViewById(R.id.buttonDot).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int curSelection = editText.getSelectionStart();
                int length = editText.getText().toString().length();
                if (curSelection < length) {
                    String content = editText.getText().toString();
                    editText.setText(content.substring(0, curSelection) + "*" + content.subSequence(curSelection, length));
                    editText.setSelection(curSelection + 1);
                } else {
                    editText.setText(editText.getText().toString() + "*");
                    editText.setSelection(editText.getText().toString().length());
                }
            }
        });

        //③给叉按键设置点击监听
        view.findViewById(R.id.buttonCross).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = editText.getText().toString().length();
                int curSelection = editText.getSelectionStart();
                if (length > 0 && curSelection > 0 && curSelection <= length) {
                    String content = editText.getText().toString();
                    editText.setText(content.substring(0, curSelection - 1) + content.subSequence(curSelection, length));
                    editText.setSelection(curSelection - 1);
                }
            }
        });
        //长按删除时间
        view.findViewById(R.id.buttonCross).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editText.setText("");
                return true;
            }
        });

    }


    public void show() {
        if (anchorView != null) {
            doRandomSortOp();
        }
    }

    private void doRandomSortOp() {
        if (anchorView == null) {
            return;
        }
            for (int i = 0; i < commonButtonIds.length; i++) {
                final Button button = anchorView.findViewById(commonButtonIds[i]);
                button.setText("" + i);
            }

    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                // No entered text so will show hint
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            } else {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public boolean onTextContextMenuItem(int id) {
        return super.onTextContextMenuItem(id);
    }
}
