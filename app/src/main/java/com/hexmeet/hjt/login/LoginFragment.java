package com.hexmeet.hjt.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.dial.RecentPreference;
import com.hexmeet.hjt.model.LoginParams;
import com.hexmeet.hjt.utils.Utils;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.LinkedList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {
    private Logger LOG = Logger.getLogger(LoginFragment.class);

    public final static int LOGIN_TYPE_PRIVATE = 1;
    public final static int LOGIN_TYPE_PRIVATE_ANONYMOUS = 2;
    public final static int LOGIN_TYPE_CLOUD = 3;
    public final static int LOGIN_TYPE_CLOUD_ANONYMOUS = 4;
    private LoginFragmentCallback callback;
    private int loginType = LOGIN_TYPE_CLOUD;
    private FormEditText input_server, input_name, input_password,  input_conf_name;
    private FormEditText input_conf_id;
    private CheckBox closeCamera, closeMic;
    private Button loginBtn;
    private LinearLayout recentContainer;

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

    public static LoginFragment newInstance(int type){
        Bundle bundle = new Bundle();
        bundle.putInt("login_type", type);
        LoginFragment oneFragment = new LoginFragment();
        oneFragment.setArguments(bundle);
        return oneFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            loginType = bundle.getInt("login_type");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.auth_login, container, false);
        mainView.findViewById(R.id.login_back).setOnClickListener(click);
        mainView.findViewById(R.id.text_advance_setting).setOnClickListener(click);
        recentContainer = (LinearLayout) mainView.findViewById(R.id.recent_list);
        loginBtn = (Button) mainView.findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(click);

        TextView title = (TextView) mainView.findViewById(R.id.login_title);
        title.setText(loginType == LOGIN_TYPE_CLOUD ? R.string.login_cloud : (loginType == LOGIN_TYPE_PRIVATE ? R.string.login_private : R.string.join_meeting));

        input_server = (FormEditText) mainView.findViewById(R.id.login_server);
        input_name = (FormEditText) mainView.findViewById(R.id.login_name);
        input_password = (FormEditText) mainView.findViewById(R.id.login_password);
        input_conf_id = (FormEditText) mainView.findViewById(R.id.login_conf_id);
        input_conf_name = (FormEditText) mainView.findViewById(R.id.login_conf_name);

        showIconDirection(false);
        input_conf_id.addTextChangedListener(watcher);
        input_conf_id.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = input_conf_id.getCompoundDrawables()[2];
                if (drawable == null){
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP){
                    return false;
                }
                if (event.getX() > input_conf_id.getWidth() - input_conf_id.getPaddingRight() - drawable.getIntrinsicWidth()){
                    forbidDefaultSoftKeyboard();
                    hideInput();
                    boolean open = v.isSelected();
                    showRecentView(!open);
                    showIconDirection(!open);
                    v.setSelected(!open);
                }else {
                    showInput(input_conf_id);
                }
                return false;
            }
        });

        closeCamera = (CheckBox) mainView.findViewById(R.id.close_camera);
        closeMic = (CheckBox) mainView.findViewById(R.id.close_mic);


        if(loginType == LOGIN_TYPE_CLOUD || loginType == LOGIN_TYPE_CLOUD_ANONYMOUS) {
            input_server.setVisibility(View.GONE);
        } else {
            mainView.findViewById(R.id.text_advance_setting).setVisibility(View.VISIBLE);
        }

        if (loginType == LOGIN_TYPE_CLOUD_ANONYMOUS || loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS) {
            loginBtn.setText(R.string.join);
            input_name.setVisibility(View.GONE);
            input_password.setVisibility(View.GONE);

            input_conf_id.setVisibility(View.VISIBLE);
            input_conf_name.setVisibility(View.VISIBLE);

            TextView subTitle = (TextView) mainView.findViewById(R.id.sub_title);
            subTitle.setVisibility(View.VISIBLE);
            subTitle.setText(loginType == LOGIN_TYPE_CLOUD_ANONYMOUS ? R.string.user_cloud : R.string.user_private);

            closeCamera.setVisibility(View.VISIBLE);
            closeMic.setVisibility(View.VISIBLE);
        }

        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    return hideKeyboard();
                }
                return false;
            }
        });
        loadPreference();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return mainView;
    }

    private void loadPreference() {
        if(loginType == LOGIN_TYPE_CLOUD) {
            input_name.setText(LoginSettings.getInstance().getUserName(true));
            input_password.setText(LoginSettings.getInstance().getPassword(true));
        } else if (loginType == LOGIN_TYPE_PRIVATE) {
            input_server.setText(LoginSettings.getInstance().getPrivateLoginServer());
            input_name.setText(LoginSettings.getInstance().getUserName(false));
            input_password.setText(LoginSettings.getInstance().getPassword(false));
        }else if(loginType == LOGIN_TYPE_CLOUD_ANONYMOUS ){
            input_conf_id.setText(LoginSettings.getInstance().getCloudNumberId());
            input_conf_name.setText(LoginSettings.getInstance().getCloudDisplayName());
            closeCamera.setChecked(LoginSettings.getInstance().isMuteVideo(true));
            closeMic.setChecked(LoginSettings.getInstance().isMuteMic(true));
        }else if(loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS){
            input_server.setText(LoginSettings.getInstance().getPrivateJoinMeetingServer());
            input_conf_id.setText(LoginSettings.getInstance().getPrivateJoinMeetingNumberId());
            input_conf_name.setText(LoginSettings.getInstance().getPrivateJoinMeetingDisplayName());
            closeCamera.setChecked(LoginSettings.getInstance().isMuteVideo(false));
            closeMic.setChecked(LoginSettings.getInstance().isMuteMic(false));
        }
        closeMic.setChecked(SystemCache.getInstance().isUserMuteMic());
        closeMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SystemCache.getInstance().setUserMuteMic(isChecked);
            }
        });
    }

    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(callback != null) {
                switch (v.getId()) {
                    case R.id.login_back:
                        hideKeyboard();
                        callback.onBackClick(LoginFragment.class.getName());
                        break;
                    case R.id.text_advance_setting:
                        callback.gotAdvanceSetting(loginType == LOGIN_TYPE_PRIVATE);
                        break;
                    case R.id.login_btn:
                        hideKeyboard();
                        if(validInput()) {
                            doLogin();
                        }
                        break;
                }
            }
        }
    };

    private void doLogin() {
        LoginSettings.getInstance().setLoginState(LoginSettings.LOGIN_STATE_IDLE, false);
        LoginParams params = new LoginParams();
        if(loginType == LOGIN_TYPE_CLOUD || loginType == LOGIN_TYPE_CLOUD_ANONYMOUS) {
            params.setServerAddress(LoginSettings.LOCATION_CLOUD);
            SystemCache.getInstance().getJoinMeetingParam().setServer(LoginSettings.LOCATION_CLOUD);
        } else {
            params.setServerAddress(input_server.getText().toString().trim());
            SystemCache.getInstance().getJoinMeetingParam().setServer(input_server.getText().toString().trim());
        }

        if (loginType == LOGIN_TYPE_CLOUD_ANONYMOUS || loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS) {
            String confIdStr = input_conf_id.getText().toString().trim();
            String numberWithoutPassword = confIdStr;
            if (confIdStr.contains("*")) {
                String[] array = confIdStr.split("\\*");
                numberWithoutPassword = array[0];
            }
            params.setNumeric_id(numberWithoutPassword);
        } else {
            params.setUser_name(input_name.getText().toString().trim());
            if(loginType == LOGIN_TYPE_CLOUD) {
                LoginSettings.getInstance().setCloudUserName(input_name.getText().toString().trim());
                LoginSettings.getInstance().setCloudPassword(input_password.getText().toString().trim());
                params.setPassword(input_password.getText().toString().trim());
            } else {
                LoginSettings.getInstance().setPrivateLoginServer(input_server.getText().toString().trim());
                LoginSettings.getInstance().setPrivateUserName(input_name.getText().toString().trim());
                LoginSettings.getInstance().setPrivatePassword(input_password.getText().toString().trim());
                params.setPassword(input_password.getText().toString().trim());
            }
        }

        String port = null;
        boolean https = BuildConfig.CLOUD_SERVER_PROTOCOL_HTTPS;

        if(!BuildConfig.CLOUD_SERVER_PORT.equals("")){
            port = BuildConfig.CLOUD_SERVER_PORT;
        }

        if(loginType == LOGIN_TYPE_PRIVATE) {
            https = LoginSettings.getInstance().useHttps();
            port = LoginSettings.getInstance().getPrivatePort();
        } else if (loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS) {
            https = SystemCache.getInstance().getJoinMeetingParam().isUseHttps();
            port = SystemCache.getInstance().getJoinMeetingParam().getPort();
        }

        if(callback != null) {
            setLoginBtnEnable(false);
            SystemCache.getInstance().setCloudLogin(loginType == LOGIN_TYPE_CLOUD);

            if(loginType == LOGIN_TYPE_CLOUD_ANONYMOUS || loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS) {
                if(TextUtils.getTrimmedLength(input_conf_name.getText().toString().trim())>16){
                    Utils.showToast(getActivity(), R.string.displayname_max_length);
                    setLoginBtnEnable(true);
                }else {
                    setAnonymousConfig();
                }
            }else {
                callback.doLogin(params, https, port);
            }

        }
    }

    private void setAnonymousConfig() {
        String number = input_conf_id.getText().toString().trim();
        String numberWithoutPassword = number;
        String password = "";
        if (number.contains("*")) {
            String[] strs = number.split("\\*");
            numberWithoutPassword = strs[0];
            password = strs[1];
        }

        String displayName = input_conf_name.getText().toString().trim();
        int trimmedLength = TextUtils.getTrimmedLength(displayName);
        LOG.info("displayName length : "+trimmedLength);
        if(TextUtils.isEmpty(displayName)) {
            displayName = Build.MODEL;
            input_conf_name.setText(displayName);
        }
        if(!Utils.regExTest(displayName)){
            Utils.showToast(getActivity(), getString(R.string.username_character,"”,<,>."));
            setLoginBtnEnable(true);
            return;
        }

        SystemCache.getInstance().getJoinMeetingParam().setConferenceNumber(numberWithoutPassword);
        SystemCache.getInstance().getJoinMeetingParam().setPassword(password);
        SystemCache.getInstance().getJoinMeetingParam().setDisplayName(displayName);
        SystemCache.getInstance().getJoinMeetingParam().setCloud(loginType == LOGIN_TYPE_CLOUD_ANONYMOUS);

        SystemCache.getInstance().setCamera(!closeCamera.isChecked());
        HjtApp.getInstance().getAppService().setVideoMode(true);
        HjtApp.getInstance().getAppService().enableVideo(!closeCamera.isChecked());
        HjtApp.getInstance().getAppService().muteMic(closeMic.isChecked());

        if(loginType == LOGIN_TYPE_CLOUD_ANONYMOUS ){
            LoginSettings.getInstance().setCloudNumberId(numberWithoutPassword);
            LoginSettings.getInstance().setCloudDisplayName(displayName);

            LoginSettings.getInstance().setCloudMuteVideo(closeCamera.isChecked());
            LoginSettings.getInstance().setCloudMuteMic(closeMic.isChecked());
        }else if(loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS){
            LoginSettings.getInstance().setPrivateJoinMeetingServer(input_server.getText().toString().trim());
            LoginSettings.getInstance().setPrivateJoinMeetingNumberId(numberWithoutPassword);
            LoginSettings.getInstance().setPrivateJoinMeetingDisplayName(displayName);
            LoginSettings.getInstance().setPrivateJoinMeetingPort(SystemCache.getInstance().getJoinMeetingParam().getPort());
            LoginSettings.getInstance().setJoinMeetingHttps(SystemCache.getInstance().getJoinMeetingParam().isUseHttps());

            LoginSettings.getInstance().setPrivateMuteVideo(closeCamera.isChecked());
            LoginSettings.getInstance().setPrivateMuteMic(closeMic.isChecked());
        }
        setLoginBtnEnable(true);
        callback.dialOut();
    }



    private boolean validInput() {
        if(loginType == LOGIN_TYPE_CLOUD) {
            return input_name.testValidity() && input_password.testValidity();
        }

        if(loginType == LOGIN_TYPE_PRIVATE) {
            return input_server.testValidity() && input_name.testValidity() && input_password.testValidity();
        }

        if(loginType == LOGIN_TYPE_PRIVATE_ANONYMOUS) {
            return input_server.testValidity() && input_conf_id.testValidity();
        } else {
            return input_conf_id.testValidity();
        }
    }

    private boolean hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        LOG.info("Soft keyboard showing? ["+imm.isActive()+"]");
        View focus = getActivity().getCurrentFocus();
        if(imm.isActive() && focus != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
        }
        return false;
    }

    public void setLoginBtnEnable(boolean enable) {
        loginBtn.setEnabled(enable);
    }

    public void showIconDirection(boolean direction){
        if(direction){
            Drawable drawables = getResources().getDrawable(R.drawable.ic_up);
            drawables .setBounds(0, 0, 40, 40);//第一个 0 是距左边距离，第二个 0 是距上边距离，40 分别是长宽
            input_conf_id .setCompoundDrawables(null , null, drawables, null);//只放左边
        }else {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_down);
            drawable .setBounds(0, 0, 40, 40);//第一个 0 是距左边距离，第二个 0 是距上边距离，40 分别是长宽
            input_conf_id .setCompoundDrawables(null , null, drawable, null);//只放左边
        }
    }

    private void showRecentView(boolean show) {
        recentContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        if(show) {
            recentContainer.removeAllViews();
            LinkedList<String> recents = RecentPreference.getInstance().getRecentList();
            if(recents.isEmpty()) {
                showEmptyRecent();
            } else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                for (int i = 0; i < recents.size(); i++) {
                    String item = recents.get(i);
                    ViewGroup child = (ViewGroup) inflater.inflate(R.layout.recent_item, recentContainer, false);
                    child.getChildAt(0).setTag(item);
                    child.getChildAt(0).setOnClickListener(recent_click);
                    ((TextView)child.getChildAt(0)).setText(item);

                    child.getChildAt(1).setTag(item);
                    child.getChildAt(1).setOnClickListener(recent_del_click);

                    if(i == (recents.size() -1)){
                        child.getChildAt(2).setVisibility(View.GONE);
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    recentContainer.addView(child, lp);
                }
            }
        }
    }

    private View.OnClickListener recent_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkAndCloseRecentView();
            String number = (String) v.getTag();
            input_conf_id.setText(number);
        }
    };

    private void checkAndCloseRecentView() {
        showIconDirection(false);
        if(recentContainer.getVisibility() == View.VISIBLE) {
            recentContainer.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener recent_del_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            recentContainer.removeView((View) v.getParent());
            String number = (String) v.getTag();
            RecentPreference.getInstance().updateRecent(number, false);
            if(recentContainer.getChildCount() == 0) {
                showEmptyRecent();
            }
        }
    };

    private void showEmptyRecent() {
        TextView emptyView = new TextView(getContext());
        emptyView.setText(R.string.empty_recent);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        emptyView.setPadding(0, 40, 0, 40);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.font_color_AB313131));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        recentContainer.addView(emptyView, lp);
    }

    public void showInput(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideInput() {
        LOG.info("hideInput");
        InputMethodManager imm = (InputMethodManager)  getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        View v =  getActivity().getWindow().peekDecorView();
        if (null != v) {
            //强制隐藏键盘
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void forbidDefaultSoftKeyboard() {
        if (input_conf_id == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT > 10) {
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(input_conf_id, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkAndCloseRecentView();
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

}
