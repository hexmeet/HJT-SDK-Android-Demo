package com.hexmeet.hjt.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

import androidx.fragment.app.Fragment;

public class MeFrag extends Fragment {
    private Logger LOG = Logger.getLogger(MeFrag.class);

    private ImageView avatar;
    private TextView username;
    private TextView displayName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.me, container, false);

        avatar = (ImageView) root.findViewById(R.id.avatar);
        username = (TextView) root.findViewById(R.id.username);
        displayName = (TextView)  root.findViewById(R.id.displayName);
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
                    //通知管理
                    /*Intent intents = new Intent();
                    intents.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intents.putExtra(Settings.EXTRA_APP_PACKAGE,getActivity().getPackageName());
                    startActivity(intents);*/
                    //权限管理
                  //  PermissionUtil.GoToSetting(getActivity());

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
            displayName.setText(getString(R.string.account)+": "+SystemCache.getInstance().getLoginResponse().getUsername());
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
