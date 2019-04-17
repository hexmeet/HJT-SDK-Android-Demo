package com.hexmeet.hjt.me;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.AppSettings;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.event.LogPathEvent;
import com.hexmeet.hjt.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParametersActivity extends BaseActivity {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ParametersActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ScreenUtil.initStatusBar(this);
        setContentView(R.layout.parameters_layout);
        EventBus.getDefault().register(this);

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        findViewById(R.id.feedback_diagnose).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HjtApp.getInstance().getAppService().obtainLogPath();
               // reportProblem();
            }
        });

        Switch autoAnswer = (Switch) findViewById(R.id.auto_answer_switch);
        autoAnswer.setChecked(AppSettings.getInstance().isAutoAnswer());
        autoAnswer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppSettings.getInstance().setAutoAnswer(isChecked);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogEvent(LogPathEvent event) {
        if(event != null){
            reportProblem(event.getPath());
        }
    }

    private void reportProblem(String sdkPath) {
        LOG.info("ParametersActivity, start sent diagnosis logs");
        String[] email = {"appsupport@cninnovatel.com"};
        Uri uri = Uri.parse("mailto:");
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE, uri);
        intent.putExtra(Intent.EXTRA_EMAIL, email); // 接收人
        intent.putExtra(Intent.EXTRA_CC, ""); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " +
                                getString(R.string.problem_diagnosis)); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.subject_content) + "\n"
                + getString(R.string.time_occurrence) + "\n" + getString(R.string.problem_des)); // 正文

        // 附件
        File file1 = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                + "/hjt_crash.log");
        File file2 = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                + "/hjt_crash.log1");
        File uilog = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                + "/hjt_app.log");

        // add sdk logs
        File fileSDK = new File(sdkPath);

        List<Uri> uris = new ArrayList<>();
        if (file1.exists()) {
            uris.add(getFileUri(file1));
        }
        if (file2.exists()) {
            uris.add(getFileUri(file2));
        }
        if (uilog.exists()) {
            uris.add(getFileUri(uilog));
        }
        if (fileSDK.exists()) {
            // create sdk log file if not existed.
            File crashdir = new File(Environment.getExternalStorageDirectory().toString() + "/crash");
            if (!crashdir.exists()) {
                crashdir.mkdirs();
            }

            // move to external directory
            File fileSDKTemp = new File(Environment.getExternalStorageDirectory().toString() + "/crash"
                    + "/hjt_sdk.gz");
            if (Utils.copyFile(fileSDK, fileSDKTemp)) {
                LOG.info("re_diagnosis onClick, move " + fileSDK.getPath() + " to " + fileSDKTemp.getPath()
                        + " succeed.");
                uris.add(getFileUri(fileSDKTemp));
            } else {
                LOG.warn("re_diagnosis onClick, move " + fileSDK.getPath() + " to " + fileSDKTemp.getPath()
                        + " failed.");
            }
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, (ArrayList<? extends Parcelable>) uris);

        // 邮件发送类型：带附件的邮件
        intent.setType("application/octet-stream");
        startActivity(Intent.createChooser(intent, getString(R.string.select_email_software)));
    }

    private Uri getFileUri(File file) {
        Uri uri;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            uri = FileProvider.getUriForFile(ParametersActivity.this, AppCons.APP_FILE_PROVIDER_AUTH, file);
        }
        return uri;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
