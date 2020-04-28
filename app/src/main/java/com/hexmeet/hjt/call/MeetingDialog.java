package com.hexmeet.hjt.call;

import android.app.Dialog;
import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;


public class MeetingDialog  extends Dialog {
    final static int MEETING_PERMISSION = 1;
    final static int MEETING_UPDATE_NAME = 2;
    final static int MEETING_UNMUTE = 3;
    final static int MEETING_LEAVE = 4;
    final static int MEETING_END = 5;

    public MeetingDialog(Context context, int theme) {
        super(context , theme);
    }

    public static class Builder {
        MeetingDialog dialog;
        private View layout;
        private TextView mMeetingTitle;
        private LinearLayout mPermissionDialog;
        private EditText mUpdateUsername;
        private TextView mMuteAndLeavemeeting;
        private Button mMeetingCancel;
        private Button mMeetingOk;
        private Button mMeetingCallend;
        private View.OnClickListener okButtonClickListener;
        private View.OnClickListener cancelButtonClickListener;
        private View.OnClickListener endButtonClickListener;
        private int type = 1;

        public Builder(Context context) {
            dialog = new MeetingDialog(context, R.style.dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.meeting_dialog_layout, null);
            dialog.setContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mMeetingTitle = (TextView) layout.findViewById(R.id.meeting_title);
            mPermissionDialog = (LinearLayout) layout.findViewById(R.id.permission_dialog);
            mUpdateUsername = (EditText) layout.findViewById(R.id.update_username);
            mMuteAndLeavemeeting = (TextView) layout.findViewById(R.id.mute_and_leavemeeting);
            mMeetingCancel = (Button) layout.findViewById(R.id.meeting_cancel);
            mMeetingOk = (Button) layout.findViewById(R.id.meeting_ok);
            mMeetingCallend = (Button) layout.findViewById(R.id.meeting_callend);
        }

        public Builder setInputWatcher(TextWatcher textWatcher) {
            mUpdateUsername.addTextChangedListener(textWatcher);
            return this;
        }


        public Builder setOKButton(View.OnClickListener listener) {
            this.okButtonClickListener = listener;
            return this;
        }

        public Builder setCancelButton(View.OnClickListener listener) {
            this.cancelButtonClickListener = listener;
            return this;
        }

        public Builder setEndButton(View.OnClickListener listener) {
            this.endButtonClickListener = listener;
            return this;
        }

        //结束会议号码
        public Builder dialogType(int type) {
            this.type = type;
            return this;
        }

        public MeetingDialog createTwoButtonDialog() {
            mMeetingOk.setOnClickListener(okButtonClickListener);
            mMeetingCancel.setOnClickListener(cancelButtonClickListener);
            mMeetingCallend.setOnClickListener(endButtonClickListener);

            switch (type){
                case MEETING_PERMISSION:
                    mMeetingTitle.setText(R.string.apply_permission);
                    setTitle(true);
                    setPermission(true);
                    setOk(true);
                    setCancel(true);

                    setCallEnd(false);
                    setUpdateUsername(false);
                    setMuteAndLeavemeeting(false);
                    break;
                case MEETING_UPDATE_NAME:
                    mMeetingTitle.setText(R.string.rename);
                    mUpdateUsername.setText(HjtApp.getInstance().getAppService().getDisplayName());
                    setTitle(true);
                    setUpdateUsername(true);
                    setOk(true);
                    setCancel(true);

                    setPermission(false);
                    setCallEnd(false);
                    setMuteAndLeavemeeting(false);
                    break;
                case MEETING_UNMUTE:
                    mMuteAndLeavemeeting.setText(R.string.audio_indication);
                    setMuteAndLeavemeeting(true);
                    setOk(true);
                    setCancel(true);

                    setPermission(false);
                    setCallEnd(false);
                    setTitle(false);
                    setUpdateUsername(false);
                    break;
                case MEETING_LEAVE:
                    mMuteAndLeavemeeting.setText(R.string.leave_meeting);
                    setMuteAndLeavemeeting(true);
                    setOk(true);
                    setCancel(true);

                    setPermission(false);
                    setCallEnd(false);
                    setTitle(false);
                    setUpdateUsername(false);
                    break;
                case MEETING_END:
                    mMeetingOk.setText(R.string.end_meeting);
                    mMuteAndLeavemeeting.setText(R.string.call_end_meeting);
                    setMuteAndLeavemeeting(true);
                    setOk(true);
                    setCancel(true);
                    setCallEnd(true);

                    setPermission(false);
                    setTitle(false);
                    setUpdateUsername(false);

                    break;
            }
            create();
            return dialog;
        }

        private void create() {
            //dialog.setContentView(layout);
            dialog.setCancelable(true);     //用户可以点击手机Back键取消对话框显示
            dialog.setCanceledOnTouchOutside(false);        //用户不能通过点击对话框之外的地方取消对话框显示
        }

        private void setTitle(boolean isGone){
            mMeetingTitle.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }

        private void setPermission(boolean isGone){
            mPermissionDialog.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }

        private void setOk(boolean isGone){
            mMeetingOk.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }

        private void setCancel(boolean isGone){
            mMeetingCancel.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }

        private void setCallEnd(boolean isGone){
            mMeetingCallend.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }

        private void setUpdateUsername(boolean isGone){
            mUpdateUsername.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }

        private void setMuteAndLeavemeeting(boolean isGone){
            mMuteAndLeavemeeting.setVisibility(isGone ? View.VISIBLE : View.GONE);
        }
    }
}
