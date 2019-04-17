package com.hexmeet.hjt.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.event.UserPasswordEvent;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EditPasswordActivity extends BaseActivity {
    private TextView commitButton;
    private EditText password_new,password_old;
    private EditText password_confirm;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, EditPasswordActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.password_layout);
        commitButton = (TextView) findViewById(R.id.password_commit);
        commitButton.setEnabled(false);

        ((TextView) findViewById(R.id.username)).setText(SystemCache.getInstance().getLoginResponse().getUsername());

        password_old= (EditText) findViewById(R.id.password_old);
        password_new = (EditText) findViewById(R.id.password_new);
        password_confirm = (EditText) findViewById(R.id.password_confirm);
        password_old.addTextChangedListener(watcher01);
        password_new.addTextChangedListener(watcher02);
        password_confirm.addTextChangedListener(watcher03);

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        commitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SystemCache.getInstance().isNetworkConnected()) {
                    Utils.showToast(EditPasswordActivity.this, R.string.network_unconnected);
                    return;
                }
                if ("".equals(password_new.getText().toString()) && "".equals(password_confirm.getText().toString())) {
                    return;
                } else if ((password_new.getText().toString().length() > 16
                        || password_new.getText().toString().length() < 4 || password_new.getText().toString()
                        .contains(" "))
                        || (password_confirm.getText().toString().length() > 16
                        || password_confirm.getText().toString().length() < 4 || password_confirm.getText()
                        .toString().contains(" "))) {
                    Utils.showToast(EditPasswordActivity.this, R.string.password_format_error);
                } else if (!password_new.getText().toString().equals(password_confirm.getText().toString()) ) {
                    Utils.showToast(EditPasswordActivity.this, R.string.passwords_donot_match);
                }else if(!TextUtils.equals(password_old.getText().toString(), LoginSettings.getInstance().getPassword(LoginSettings.getInstance().isCloudLoginSuccess())) ){
                    Utils.showToast(getBaseContext(), R.string.error_password);
                }else if(TextUtils.isEmpty(password_old.getText().toString())){
                    Utils.showToast(getBaseContext(), R.string.password_not_empty);
                } else {
                    commitButton.setEnabled(false);
                    LOG.info("EditPasswordActivity, update password");
                    HjtApp.getInstance().getAppService().updatePassword(password_old.getText().toString(),password_new.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    TextWatcher watcher01 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!"".equals(password_old.getText().toString())) {
                commitButton.setEnabled(true);
            } else {
                commitButton.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    TextWatcher watcher02 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!"".equals(password_new.getText().toString()) && !"".equals(password_confirm.getText().toString())) {
                commitButton.setEnabled(true);
            } else {
                commitButton.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    TextWatcher watcher03 = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!"".equals(password_new.getText().toString()) && !"".equals(password_confirm.getText().toString())) {
                commitButton.setEnabled(true);
            } else {
                commitButton.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserPasswordEvent(UserPasswordEvent event) {
        if(event.isSuccess()) {
            Utils.showToast(this, R.string.update_password_success);
            onBackPressed();
        } else {
            commitButton.setEnabled(true);
            Utils.showToast(this, R.string.update_password_failed);
            LOG.error("User rename failed: ["+event.getMessage()+"]");
        }
    }

}
