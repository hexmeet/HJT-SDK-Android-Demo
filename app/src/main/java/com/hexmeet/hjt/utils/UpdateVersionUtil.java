package com.hexmeet.hjt.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hexmeet.hjt.R;

public class UpdateVersionUtil extends Dialog {

    public UpdateVersionUtil(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private UpdateVersionUtil dialog;
        private View layout;
        private View.OnClickListener cancelButtonClickListener;
        private View.OnClickListener okButtonClickListener;
        String version;

        public Builder(final Context context) {
            dialog = new UpdateVersionUtil(context, R.style.dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.alertdialog_verstion, null);
            dialog.setContentView(layout);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setCancelButton(View.OnClickListener listener) {
            this.cancelButtonClickListener = listener;
            return this;
        }

        public Builder setOkButton(View.OnClickListener listener) {
            this.okButtonClickListener = listener;
            return this;
        }

        public UpdateVersionUtil createTwoButtonDialog() {
            layout.findViewById(R.id.version_cancel).setOnClickListener(cancelButtonClickListener);
            layout.findViewById(R.id.version_ok).setOnClickListener(okButtonClickListener);

            if(version != null){
                ((TextView) layout.findViewById(R.id.version_number)).setText(version);
            }
            return dialog;
        }
    }
}
