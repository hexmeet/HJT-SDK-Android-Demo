package com.hexmeet.hjt.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.utils.Utils;

public class AboutActivity extends BaseActivity {

    private RelativeLayout serviceTerms;
    private RelativeLayout servicePrivacyPolicy;

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
        serviceTerms = (RelativeLayout)findViewById(R.id.service_terms);
        servicePrivacyPolicy = (RelativeLayout)findViewById(R.id.service_privacy_policy);

        if(BuildConfig.HIDE_USER_LICENSE){
            serviceTerms.setVisibility(View.GONE);
            servicePrivacyPolicy.setVisibility(View.GONE);
        }

        serviceTerms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, ServiceTermsActivity.class);
                intent.putExtra(AppCons.ISTERMSOFSERVICE,true);
                startActivity(intent);
            }
        });

        servicePrivacyPolicy.setOnClickListener(new OnClickListener() {
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

}
