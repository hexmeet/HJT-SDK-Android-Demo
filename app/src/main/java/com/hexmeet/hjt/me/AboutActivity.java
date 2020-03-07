package com.hexmeet.hjt.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.utils.Utils;

public class AboutActivity extends BaseActivity {

    private ImageView logo;

    private final int CLICK_NUM = 6;//点击6次
    private final int CLICK_INTERVER_TIME = 3000;//点击时间间隔5秒
    private long lastClickTime = 0; //上一次的点击时间
    private int clickNum = 0;//记录点击次数

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_hexmeet);

        TextView version = (TextView) findViewById(R.id.version);
        version.setText(Utils.getVersion());
        LOG.info("APP version : "+Utils.getVersion());
        TextView copyright1 = (TextView) findViewById(R.id.copyright1);
        copyright1.setTextSize(HjtApp.isEnVersion() ? 10 : 12);
        TextView copyright2 = (TextView) findViewById(R.id.copyright2);
        copyright2.setTextSize(HjtApp.isEnVersion() ? 10 : 12);
        findViewById(R.id.version_remind).setVisibility(SystemCache.getInstance().isShowRemind()? View.VISIBLE : View.INVISIBLE);
        logo = (ImageView) findViewById(R.id.logo);
        if(!SystemCache.getInstance().isVisibilitySharedScreen()){
            logo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    nineClick();
                }
            });
        }



        findViewById(R.id.service_terms).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, ServiceTermsActivity.class);
                intent.putExtra(AppCons.ISTERMSOFSERVICE,true);
                startActivity(intent);
            }
        });

        findViewById(R.id.service_privacy_policy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, ServiceTermsActivity.class);
                intent.putExtra(AppCons.ISTERMSOFSERVICE,false);
                startActivity(intent);
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.version_about).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVersion(true);
            }
        });
    }


    public void nineClick() { //点击的间隔时间不能超过3秒
        long currentClickTime = SystemClock.uptimeMillis();
        if (currentClickTime - lastClickTime <= CLICK_INTERVER_TIME || lastClickTime == 0) {
            lastClickTime = currentClickTime;
            clickNum = clickNum + 1;
        } else {//超过5秒的间隔 //重新计数 从1开始
            clickNum = 1;
            lastClickTime = 0;
            return;
        }
        if (clickNum == CLICK_NUM) {//重新计数
            LOG.info("onclick five time");
            clickNum = 0;
            lastClickTime = 0;
            logo.setClickable(false);//禁用点击事件
            SystemCache.getInstance().setVisibilitySharedScreen(true);
            Utils.showToast(AboutActivity.this, R.string.open_share);
        }
    }

}
