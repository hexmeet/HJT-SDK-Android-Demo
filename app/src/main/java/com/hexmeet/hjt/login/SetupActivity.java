package com.hexmeet.hjt.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.me.AboutActivity;
import com.hexmeet.hjt.me.ParametersActivity;

public class SetupActivity extends BaseActivity implements View.OnClickListener {
    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SetupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        findViewById(R.id.setup_setting).setOnClickListener(this);
        findViewById(R.id.setup_about).setOnClickListener(this);
        findViewById(R.id.setup_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setup_setting:
                Intent intent = new Intent(SetupActivity.this, ParametersActivity.class);
               startActivity(intent);
                break;
            case R.id.setup_about:
                Intent aboutintent = new Intent(SetupActivity.this, AboutActivity.class);
                startActivity(aboutintent);
                break;
            case R.id.setup_btn:
                onBackPressed();
                break;

            default:
                break;
        }
    }
}
