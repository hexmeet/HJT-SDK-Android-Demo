package com.hexmeet.hjt.login;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;

public class MainLoginFragment extends Fragment {
    private LoginFragmentCallback callback;

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

    public static MainLoginFragment newInstance(){
        MainLoginFragment oneFragment = new MainLoginFragment();
        return oneFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.login_main, container, false);
        mainView.findViewById(R.id.login_cloud).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null) {
                    callback.gotoCloudLogin();
                }
            }
        });

        mainView.findViewById(R.id.login_private).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null) {
                    callback.gotoPrivateLogin();
                }
            }
        });
        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if(HjtApp.isEnVersion()) {
                ((ImageView)getView().findViewById(R.id.logo_pic)).setImageResource(R.drawable.text_logo_en);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
