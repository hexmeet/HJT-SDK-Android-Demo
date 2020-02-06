package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.alibaba.sdk.android.push.AndroidPopupActivity;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.me.ServiceTermsActivity;
import com.hexmeet.hjt.utils.AlertDialogUtil;
import com.hexmeet.hjt.utils.ResourceUtils;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class SplashActivity extends BaseActivity {
    private final int TIMEOUT = -100;
    private final int WAIT_SERVICE = 100;
    private AlertDialogUtil dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("App splash onCreate");
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        if ((HjtApp.isCnVersion() && BuildConfig.SHOW_PRIVACY_WINDOW) && (!LoginSettings.getInstance().getPrivacyPolicy() && !SystemCache.getInstance().isInviteMakeCall())){
            dialog = new AlertDialogUtil.Builder(SplashActivity.this)
                    .setPositiveButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//同意
                            LoginSettings.getInstance().setPrivacyPolicy(true);
                            dialog.dismiss();
                            autoLoginHandler.sendEmptyMessage(WAIT_SERVICE);
                        }
                    }).setNegativeButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//取消
                            dialog.dismiss();
                            finish();
                        }
                    }).setServiceIntent(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//服务协议
                            Intent intent = new Intent(SplashActivity.this, ServiceTermsActivity.class);
                            intent.putExtra(AppCons.ISTERMSOFSERVICE,true);
                            startActivity(intent);
                        }
                    }).setPrivacyIntent(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//隐私政策
                            Intent intent = new Intent(SplashActivity.this, ServiceTermsActivity.class);
                            intent.putExtra(AppCons.ISTERMSOFSERVICE,false);
                            startActivity(intent);
                        }
                    }).createTwoButtonDialog();
            dialog.show();
        }else {
            autoLoginHandler.sendEmptyMessage(WAIT_SERVICE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LOG.info("App splash onStart");
    }

    @SuppressLint("HandlerLeak")
    private Handler autoLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIMEOUT) {
                LOG.info("splash timeout after 1 seconds");
                Login.actionStart(SplashActivity.this);
                finish();
            }

            if (msg.what == WAIT_SERVICE) {
                if(HjtApp.getInstance().getAppService() != null && SystemCache.getInstance().isSdkReady()) {
                    if(PermissionWrapper.getInstance().checkStoragePermission(SplashActivity.this)) {
                        HjtApp.getInstance().initLogs();
                        turnPage();
                    }
                } else {
                    LOG.info("Sdk|AppService not ready");
                    sendEmptyMessageDelayed(WAIT_SERVICE, 1000);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            turnPage();
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
             finish();
        }
    }

    private void turnPage() {
        ResourceUtils.getInstance().initScreenSize();
        autoLoginHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Login.actionStart(SplashActivity.this);
                finish();
            }
        },1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOG.info(" onDestroy()");
    }



    private  void  showPrivacyWindow(){
       /* AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCancelable(false);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Window window = dialog.getWindow();

        window.setContentView(R.layout.alertdialog_show_privacy);
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.show();*/
       /* final EditText  mNewUsername = (EditText) window.findViewById(R.id.new_username);
        Button  mUpdateNameCancel = (Button) window.findViewById(R.id.update_name_cancel);
        Button mUpdateNameOk = (Button) window.findViewById(R.id.update_name_ok);
        mNewUsername.setText(HjtApp.getInstance().getAppService().getDisplayName());
        mNewUsername.setFocusable(true);
        mNewUsername.setFocusableInTouchMode(true);
        mNewUsername.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);*/

        /*mUpdateNameOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mNewUsername.getText().toString().trim();
                if(!displayName.equals("") && !Utils.regExTest(displayName)){
                    LOG.info(" new displayName : "+displayName);
                    HjtApp.getInstance().getAppService().setConfDisplayName(displayName);
                    videoBoxGroup.updateLocalName(displayName);
                    updateUserNameDialog.cancel();
                }else {
                    Utils.showToastWithCustomLayout(Conversation.this, getString(R.string.error_form));
                }

            }
        });
        mUpdateNameCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserNameDialog.cancel();
            }
        });*/





        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        final AlertDialog dialog = builder.create();
        View contentView = View.inflate(SplashActivity.this, R.layout.alertdialog_show_privacy, null);
        dialog.setView(contentView);
        //4.可以执行逻辑操作,,,按钮的监听,,,找控件还是必须通过,视图对象去找
       /* Button btn = (Button) contentView.findViewById(R.id.btn);
        final EditText edit_text = (EditText) contentView.findViewById(R.id.edit_text);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, edit_text.getText().toString(), 0).show();
                //在自定义对话框中,,,可以进行手动的关闭对话框
                dialog.dismiss();
            }
        });*/
        //5.显示对话框
        dialog.show();
    }


    class PopupPushActivity  extends AndroidPopupActivity {

        @Override
        protected void onSysNoticeOpened(String title, String content, Map<String, String> extraMap) {
            LOG.info("Receive ThirdPush notification, title: " + title + ", content: " + content + ", extraMap: " + extraMap);
            Intent intent = new Intent(this,SplashActivity.class);
            startActivity(intent);
        }
    }

}