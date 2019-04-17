package com.hexmeet.hjt.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;

public class PreLoginFragment extends Fragment {
    private LoginFragmentCallback callback;
    private int loginType = AppCons.LoginType.LOGIN_TYPE_CLOUD;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (LoginFragmentCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public static PreLoginFragment newInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt("login_type", type);
        PreLoginFragment oneFragment = new PreLoginFragment();
        oneFragment.setArguments(bundle);
        return oneFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            loginType = bundle.getInt("login_type");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (HjtApp.isEnVersion()) {
                ((ImageView) getView().findViewById(R.id.logo_pic)).setImageResource(R.drawable.text_logo_en);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.login_pre, container, false);
        mainView.findViewById(R.id.login_back).setOnClickListener(click);
        mainView.findViewById(R.id.login_join_meeting).setOnClickListener(click);
        mainView.findViewById(R.id.login_btn).setOnClickListener(click);

        TextView title = (TextView) mainView.findViewById(R.id.sub_title);
        TextView setup =(TextView) mainView.findViewById(R.id.app_setup);
        setup.setOnClickListener(click);
        title.setText(loginType == AppCons.LoginType.LOGIN_TYPE_CLOUD ? R.string.user_cloud : R.string.user_private);
        if(loginType == AppCons.LoginType.LOGIN_TYPE_CLOUD) {
            View trialView = mainView.findViewById(R.id.apply_use);
            View appview = mainView.findViewById(R.id.app_view);
            appview.setVisibility(View.VISIBLE);
            trialView.setVisibility(View.VISIBLE);
            trialView.setOnClickListener(click);
        }
        return mainView;
    }

    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (callback != null) {
                switch (v.getId()) {
                    case R.id.login_back:
                        callback.onBackClick(PreLoginFragment.class.getName());
                        break;
                    case R.id.login_join_meeting:
                        callback.gotoLoginDetail(loginType == AppCons.LoginType.LOGIN_TYPE_CLOUD ? LoginFragment.LOGIN_TYPE_CLOUD_ANONYMOUS : LoginFragment.LOGIN_TYPE_PRIVATE_ANONYMOUS);
                        break;
                    case R.id.login_btn:
                        callback.gotoLoginDetail(loginType == AppCons.LoginType.LOGIN_TYPE_CLOUD ? LoginFragment.LOGIN_TYPE_CLOUD : LoginFragment.LOGIN_TYPE_PRIVATE);
                        break;
                    case R.id.apply_use:
                        String url = BuildConfig.TRIAL_APPLICATION_URL;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        break;
                    case R.id.app_setup:
                        SetupActivity.actionStart(getActivity());
                        break;
                    default:
                        break;
                }
            }
        }
    };
}
