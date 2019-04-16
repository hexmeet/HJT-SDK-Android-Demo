package com.hexmeet.hjt.me;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.PasswordDialog;
import com.hexmeet.hjt.event.FileMessageEvent;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.login.LoginSettings;
import com.hexmeet.hjt.utils.Utils;
import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class MeFrag extends Fragment {
    private Logger LOG = Logger.getLogger(MeFrag.class);

    private ImageView avatar;
    private TextView username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.me, container, false);

        avatar = (ImageView) root.findViewById(R.id.avatar);
        username = (TextView) root.findViewById(R.id.username);
        root.findViewById(R.id.me_info).setOnClickListener(item_click);
        root.findViewById(R.id.me_setting).setOnClickListener(item_click);
        root.findViewById(R.id.me_share).setOnClickListener(item_click);
        root.findViewById(R.id.me_password).setOnClickListener(item_click);
        root.findViewById(R.id.me_about).setOnClickListener(item_click);
        return root;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private OnClickListener item_click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.me_info:
                    Intent intent = new Intent(getActivity(), MeDetailActivity.class);
                    startActivityForResult(intent, 11);
                    break;
                case R.id.me_setting:
                    ParametersActivity.actionStart(getActivity());
                    break;
                case R.id.me_password:
                    EditPasswordActivity.actionStart(getActivity());
                    break;
                case R.id.me_share:
                    InviteFriendsActivity.actionStart(getActivity());
                    break;
                case R.id.me_about:
                    AboutActivity.actionStart(getActivity());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Utils.loadAvatar(avatar);
        if (SystemCache.getInstance().getLoginResponse() != null) {
            username.setText(SystemCache.getInstance().getLoginResponse().getDisplayName());
        }
    }

    public void loadAvatar() {
        if(avatar != null) {
            Utils.loadAvatar(avatar);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOG.info("onActivityResult requestCode: ["+requestCode+"]  resultCode: ["+resultCode+"]");
        if(requestCode == 11 && resultCode == 13) {
            Login.actionStart(getActivity());
            getActivity().finish();
        }
    }
}
