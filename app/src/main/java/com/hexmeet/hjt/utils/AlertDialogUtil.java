package com.hexmeet.hjt.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.me.ServiceTermsActivity;

import androidx.annotation.NonNull;

public class AlertDialogUtil extends Dialog {

    public AlertDialogUtil(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private View layout;
        private AlertDialogUtil dialog;
        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;
        ForegroundColorSpan colorSpan;

        public Builder(final Context context) {
            dialog = new AlertDialogUtil(context, R.style.dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.alertdialog_show_privacy, null);
            TextView title = (TextView) layout.findViewById(R.id.title_dialog);
            TextView content = (TextView)layout.findViewById(R.id.privacy_policy_one);
            TextView content_one = (TextView)layout.findViewById(R.id.privacy_policy_two);
            title.setText(context.getString(R.string.app_name)+context.getString(R.string.privacy_policy));
            content.setText(context.getString(R.string.privacy_policy_one,context.getString(R.string.app_name),context.getString(R.string.app_name)));

            SpannableStringBuilder sb=new SpannableStringBuilder();
            sb.append(content_one.getText());

            ClickableSpan clickableSpan= new  ClickableSpan(){
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(context, ServiceTermsActivity.class);
                    intent.putExtra(AppCons.ISTERMSOFSERVICE,true);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            sb.setSpan(clickableSpan, 51, 62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            colorSpan=new ForegroundColorSpan(content.getResources().getColor(R.color.Blue));
            sb.setSpan(colorSpan,51, 62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


            ClickableSpan colorSpan1= new  ClickableSpan(){
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(context, ServiceTermsActivity.class);
                    intent.putExtra(AppCons.ISTERMSOFSERVICE,false);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            sb.setSpan(colorSpan1,63,69, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            colorSpan=new ForegroundColorSpan(content.getResources().getColor(R.color.Blue));
            sb.setSpan(colorSpan,63,69, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            content_one.setText(sb);
            content_one.setMovementMethod(LinkMovementMethod.getInstance());
            content_one.setHighlightColor(Color.TRANSPARENT);//点击文字设置透明
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

        public AlertDialogUtil createTwoButtonDialog() {
            layout.findViewById(R.id.dialog_ok).setOnClickListener(positiveButtonClickListener);
            layout.findViewById(R.id.dialog_cancel).setOnClickListener(negativeButtonClickListener);
            return dialog;
        }

    }
}
