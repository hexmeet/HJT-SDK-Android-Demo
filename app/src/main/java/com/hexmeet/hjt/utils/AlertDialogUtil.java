package com.hexmeet.hjt.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.call.PasswordDialog;

public class AlertDialogUtil extends Dialog {


    public AlertDialogUtil(Context context) {
        super(context);
    }
    public AlertDialogUtil(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private View layout;
        private AlertDialogUtil dialog;
        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;
        private View.OnClickListener serviceClickListener;
        private View.OnClickListener privacyClickListener;

        public Builder(Context context) {
            dialog = new AlertDialogUtil(context, R.style.dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.alertdialog_show_privacy, null);
            TextView title = (TextView) layout.findViewById(R.id.title_dialog);
            TextView content = (TextView)layout.findViewById(R.id.privacy_policy_one);
            title.setText(context.getString(R.string.app_name)+context.getString(R.string.privacy_policy));
            content.setText(context.getString(R.string.privacy_policy_one,context.getString(R.string.app_name),context.getString(R.string.app_name)));
            dialog.setContentView(layout);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        public Builder setPositiveButton(View.OnClickListener listener) {
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(View.OnClickListener listener) {
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setServiceIntent(View.OnClickListener listener) {
            this.serviceClickListener = listener;
            return this;
        }

        public Builder setPrivacyIntent(View.OnClickListener listener) {
            this.privacyClickListener = listener;
            return this;
        }

        public AlertDialogUtil createTwoButtonDialog() {
            layout.findViewById(R.id.dialog_ok).setOnClickListener(positiveButtonClickListener);
            layout.findViewById(R.id.dialog_cancel).setOnClickListener(negativeButtonClickListener);
            layout.findViewById(R.id.service_dialog).setOnClickListener(serviceClickListener);
            layout.findViewById(R.id.privacy_dialog).setOnClickListener(privacyClickListener);
            return dialog;
        }

    }
}
