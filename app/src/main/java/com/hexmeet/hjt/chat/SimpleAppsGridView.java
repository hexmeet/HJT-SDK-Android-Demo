package com.hexmeet.hjt.chat;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.call.P2pCallActivity;

public class SimpleAppsGridView extends RelativeLayout {

    protected View view;
    private ImageView mIvIcon;
    private TextView mTvName;
    private ImageView mIvVideo;
    private TextView mTvVideo;
    public SimpleAppsGridView(Context context) {
        this(context, null);
    }

    public SimpleAppsGridView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_apps, this);



        mIvIcon = (ImageView) findViewById(R.id.iv_icon);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mIvVideo = (ImageView) findViewById(R.id.iv_video);
        mTvVideo = (TextView) findViewById(R.id.tv_video);

        mIvVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClass(context, P2pCallActivity.class);
                context.startActivity(intent);
            }
        });
    }


}