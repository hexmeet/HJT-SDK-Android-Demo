package com.hexmeet.hjt.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.MultiClick;

public class AdvanceLoginFragment extends Fragment {
    private LoginFragmentCallback callback;
    private boolean privateLogin = true;
    private EditText inputPort;
    private Switch switchHttps;
    private boolean forTest = false;

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

    public static AdvanceLoginFragment newInstance(boolean privateLogin){
        Bundle bundle = new Bundle();
        bundle.putInt("login_pre", privateLogin ? 1 : 0);
        AdvanceLoginFragment oneFragment = new AdvanceLoginFragment();
        oneFragment.setArguments(bundle);
        return oneFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            privateLogin = (bundle.getInt("login_pre") == 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.advance_login_setting, container, false);
        mainView.findViewById(R.id.login_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if(callback != null) {
                    callback.onBackClick(AdvanceLoginFragment.class.getName());
                }
            }
        });

        inputPort = (EditText) mainView.findViewById(R.id.login_port);
        switchHttps = (Switch) mainView.findViewById(R.id.https_switch);
        final EditText testIp = (EditText) mainView.findViewById(R.id.test_web_ip);

        mainView.findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null) {
                    if(privateLogin) {
                        if(forTest) {
                            String ip = testIp.getText().toString().trim();
                            if(TextUtils.isEmpty(ip)) {
                                LoginSettings.getInstance().setTestServer(null);
                            } else {
                                LoginSettings.getInstance().setTestServer(ip + ":" + inputPort.getText().toString().trim());
                            }
                        } else {
                            LoginSettings.getInstance().setPrivatePort(inputPort.getText().toString().trim());
                            LoginSettings.getInstance().setUseHttps(switchHttps.isChecked());
                        }
                    } else {
                        SystemCache.getInstance().getJoinMeetingParam().setPort(inputPort.getText().toString().trim());
                        SystemCache.getInstance().getJoinMeetingParam().setUseHttps(switchHttps.isChecked());
                    }
                    callback.onBackClick(AdvanceLoginFragment.class.getName());
                }
            }
        });
        loadPreference();
        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    return hideKeyboard();
                }
                return false;
            }
        });

        if(privateLogin) {
            mainView.findViewById(R.id.login_title).setOnClickListener(new MultiClick(5) {
                @Override
                public void onFinalClick() {
                    if (forTest) {
                        testIp.setVisibility(View.GONE);
                        forTest = false;
                    } else {
                        testIp.setVisibility(View.VISIBLE);
                        forTest = true;
                        if (TextUtils.isEmpty(testIp.getText().toString()) && !TextUtils.isEmpty(LoginSettings.getInstance().getTestServer())) {
                            if(LoginSettings.getInstance().getTestServer().contains(":")) {
                                String[] strs = LoginSettings.getInstance().getTestServer().split(":");
                                testIp.setText(strs[0]);
                                if(strs.length > 1) {
                                    inputPort.setText(strs[1]);
                                }
                            } else {
                                testIp.setText(LoginSettings.getInstance().getTestServer());
                            }
                        }
                    }
                }
            });
        }
        return mainView;
    }

    private void loadPreference() {
        if(privateLogin) {
            inputPort.setText(LoginSettings.getInstance().getPrivatePort());
            switchHttps.setChecked(LoginSettings.getInstance().useHttps());
        } else {
            /*if(TextUtils.isEmpty(SystemCache.getInstance().getJoinMeetingParam().getPort())) {
                inputPort.setText("");
            } else {
                inputPort.setText(SystemCache.getInstance().getJoinMeetingParam().getPort());
            }*/
            inputPort.setText(LoginSettings.getInstance().getPrivateJoinMeetingPort());
            switchHttps.setChecked(LoginSettings.getInstance().getJoinMeetingHttps());
        }
    }

    private boolean hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = getActivity().getCurrentFocus();
        if(imm.isActive() && focus != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
        }
        return false;
    }
}
