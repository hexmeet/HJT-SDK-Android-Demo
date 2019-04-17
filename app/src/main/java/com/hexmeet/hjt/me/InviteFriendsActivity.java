package com.hexmeet.hjt.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.conf.WeChat;

public class InviteFriendsActivity extends BaseActivity {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, InviteFriendsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ScreenUtil.initStatusBar(this);
        setContentView(R.layout.invite_friends);

        TextView wechat_label = (TextView) findViewById(R.id.wechat_label);
        wechat_label.setTextSize(HjtApp.isEnVersion() ? 12 : 13);
        TextView friend_label = (TextView) findViewById(R.id.friend_label);
        friend_label.setTextSize(HjtApp.isEnVersion() ? 12 : 13);

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        wechat_label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享到微信好友
                WeChat.share(InviteFriendsActivity.this, 0);
            }
        });

        friend_label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享到微信朋友圈
                WeChat.share(InviteFriendsActivity.this, 1);
            }
        });
    }

}
