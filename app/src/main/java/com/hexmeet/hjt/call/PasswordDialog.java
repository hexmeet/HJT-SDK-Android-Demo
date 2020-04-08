package com.hexmeet.hjt.call;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;

public class PasswordDialog extends Dialog {


    public PasswordDialog(Context context) {
        super(context);
    }
    public PasswordDialog(Context context, int theme) {
        super(context , theme);
    }
    private static EditText inputBox;

    public static class Builder {
        private String title;
        private String initValue;
        private String positiveButtonText;
        private String negativeButtonText;
        private String meetingNumber;
        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;
        private View.OnClickListener okButtonClickListener;

        private View layout;
        private PasswordDialog dialog;

        private TextView messageText;
        private boolean isPasswordDialog = true;
        private boolean showNetwork = true;
        private final LinearLayout callEndLayout;
        private final LinearLayout updateNameLayout;
        private  TextView number;
        private  TextView callEndText;

        public Builder(Context context) {
            //这里传入自定义的style，直接影响此Dialog的显示效果。style具体实现见style.xml
            dialog = new PasswordDialog(context, R.style.PasswordDialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.custom_dialog_layout, null);
            dialog.setContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            inputBox = (EditText) layout.findViewById(R.id.input_password);
            messageText = (TextView) layout.findViewById(R.id.dialog_message);
            callEndLayout = (LinearLayout) layout.findViewById(R.id.call_end_layout);
            updateNameLayout = (LinearLayout) layout.findViewById(R.id.update_name_layout);
            number =(TextView)  layout.findViewById(R.id.meeting_number);
            callEndText = (TextView) layout.findViewById(R.id.call_end_dialog);
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

        //修改文字或提示挂断
        public Builder setPasswordDialog(boolean isPasswordDialog) {
            this.isPasswordDialog = isPasswordDialog;
            return this;
        }

        //会议结束 点击确定
        public Builder setOkButton(View.OnClickListener listener) {
            this.okButtonClickListener = listener;
            return this;
        }
        //结束会议号码
        public Builder setNumber(String number) {
            this.meetingNumber = number;
            return this;
        }
        //是否显示网络异常或者主持人挂断
        public Builder setNetwork(boolean network) {
            this.showNetwork = network;
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
            layout.findViewById(R.id.call_ok).setOnClickListener(okButtonClickListener);
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
            if(isPasswordDialog){//是否显示修改名字layout
                callEndLayout.setVisibility(View.GONE);
                updateNameLayout.setVisibility(View.VISIBLE);
            }else {
                callEndLayout.setVisibility(View.VISIBLE);
                updateNameLayout.setVisibility(View.GONE);
            }

            if(showNetwork){
                callEndText.setText(HjtApp.getInstance().getContext().getString(R.string.end_of_meeting));
            }else {
                callEndText.setText(HjtApp.getInstance().getContext().getString(R.string.network_exception));
            }

            if(meetingNumber != null ){
                number.setText(meetingNumber);
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(number.getText());
                builder.setSpan(new StyleSpan(Typeface.BOLD),0,number.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //加粗
                number.setText(builder);
                number.setVisibility(View.VISIBLE);
            }else {
                number.setVisibility(View.GONE);
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

    public  void setName(String userName){
        inputBox.setText(userName);
    }
    public void clean(){
        if(inputBox!=null){
            inputBox = null;
        }
    }
}
