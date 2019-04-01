package com.hexmeet.hjt.call;

import android.app.Dialog;
import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hexmeet.hjt.R;

public class PasswordDialog extends Dialog {

    public PasswordDialog(Context context) {
        super(context);
    }
    public PasswordDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private String title;
        private String initValue;
        private String positiveButtonText;
        private String negativeButtonText;
        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;

        private View layout;
        private PasswordDialog dialog;
        private EditText inputBox;
        private TextView messageText;

        public Builder(Context context) {
            //这里传入自定义的style，直接影响此Dialog的显示效果。style具体实现见style.xml
            dialog = new PasswordDialog(context, R.style.PasswordDialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.custom_dialog_layout, null);
            dialog.setContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            inputBox = (EditText) layout.findViewById(R.id.input_password);
            messageText = (TextView) layout.findViewById(R.id.dialog_message);
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            if(message != null) {
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message);
            } else {
                messageText.setVisibility(View.GONE);
            }
            return this;
        }

        public Builder setInitValue(String initValue, int inputType, String hint) {
            this.initValue = initValue;
            inputBox.setInputType(inputType);
            inputBox.setHint(hint);
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, View.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, View.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setInputWatcher(TextWatcher textWatcher) {
            inputBox.addTextChangedListener(textWatcher);
            return this;
        }


        /**
         * 创建双按钮对话框
         *
         * @return
         */
        public PasswordDialog createTwoButtonDialog() {
            layout.findViewById(R.id.positiveButton).setOnClickListener(positiveButtonClickListener);
            layout.findViewById(R.id.negativeButton).setOnClickListener(negativeButtonClickListener);
            //如果传入的按钮文字为空，则使用默认的“是”和“否”
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
            }
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
            }
            if(title != null){
                ((TextView) layout.findViewById(R.id.dialog_title)).setText(title);
            }

            if(initValue != null) {
                inputBox.setText(initValue);
            }
            create();
            return dialog;
        }

        /**
         * 单按钮对话框和双按钮对话框的公共部分在这里设置
         */
        private void create() {
            //dialog.setContentView(layout);
            dialog.setCancelable(true);     //用户可以点击手机Back键取消对话框显示
            dialog.setCanceledOnTouchOutside(false);        //用户不能通过点击对话框之外的地方取消对话框显示
        }
    }
}
