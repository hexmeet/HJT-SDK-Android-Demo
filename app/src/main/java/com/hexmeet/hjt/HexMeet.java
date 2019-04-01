package com.hexmeet.hjt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.AnonymousJoinMeetActivity;
import com.hexmeet.hjt.call.ConnectActivity;
import com.hexmeet.hjt.call.Conversation;
import com.hexmeet.hjt.conf.ConferenceListFrag;
import com.hexmeet.hjt.conf.JsJoinMeeting;
import com.hexmeet.hjt.dial.DialingFrag;
import com.hexmeet.hjt.event.FileMessageEvent;
import com.hexmeet.hjt.event.LoginResultEvent;
import com.hexmeet.hjt.event.ServerReachableEvent;
import com.hexmeet.hjt.login.JoinMeetingParam;
import com.hexmeet.hjt.login.Login;
import com.hexmeet.hjt.me.MeFrag;
import com.hexmeet.hjt.utils.JsonUtil;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.ResourceUtils;
import com.hexmeet.hjt.utils.ScreenUtil;
import com.hexmeet.hjt.utils.Utils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ev.common.EVFactory;

public class HexMeet extends BaseActivity implements OnClickListener {
    public FragmentManager fragmentManager;
    public ConferenceListFrag conferenceListFrag;
    public DialingFrag dialingFrag;
    public MeFrag meFrag;

    private TextView tabConference;
    private TextView tabDialing;
    private TextView tabMe;

    private ViewGroup tabLayout;
    private View dividerLine;

    public HexMeetTab currentTab = HexMeetTab.CONFERENCE;

    private BroadcastReceiver screenReceiver;
    private boolean screenReceiverRegistered = false;
    private RelativeLayout.LayoutParams contentLp;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, HexMeet.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, Uri data) {
        Intent intent = new Intent(context, HexMeet.class);
        intent.setData(data);
        context.startActivity(intent);
    }

    public static void actionStartAsInvite(Context context, int type) {
        Intent intent = new Intent(context, HexMeet.class);
        intent.putExtra(AppCons.INTENT_KEY_WEB_INVITE, type);
        context.startActivity(intent);
    }

    private void updateTip() {
        updateTip(SystemCache.getInstance().getRegisterState(), NetworkUtil.isCloudReachable());
    }

    private void updateTip(RegisterState state, boolean cloudReachable) {
        boolean isNetworkConnect = NetworkUtil.isNetConnected(HexMeet.this);
        SystemCache.getInstance().setNetworkConnected(isNetworkConnect);
        int warnRes = -1;
        if(!isNetworkConnect) {
            warnRes = R.string.network_unconnected;
        } else if (!cloudReachable) {
            warnRes = R.string.cloud_unreachable;
        } else if(state != RegisterState.SUCCESS) {
            if(!SystemCache.getInstance().isRegServerConnect()) {
              //  warnRes = R.string.reg_unreachable;
            } else {
              //  warnRes = R.string.unregistered;
            }
        }
        //conferenceListFrag.setNetworkTipVisible(warnRes != -1);
        if(warnRes != -1) {
            //conferenceListFrag.getNetworkStatus().setText(warnRes);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTab", currentTab.getTabName());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (savedInstanceState != null && savedInstanceState.getString("currentTab") != null) {
            currentTab = HexMeetTab.fromTabName(savedInstanceState.getString("currentTab"));
        }
        onCreateWrap();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onCreateWrap() {
        if(failedCheckToken()) {
            return;
        }
        setContentView(com.hexmeet.hjt.R.layout.hexmeet);
        //ScreenUtil.initStatusBar(this);
        fragmentManager = getSupportFragmentManager();
        initViews();
        //NetworkUtil.scheduleUcmDetector();

        showTab(currentTab);

        screenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(screenReceiver, filter);
        screenReceiverRegistered = true;

        HjtApp.getInstance().getAppService().setUserInLogin(true);

        if(PermissionWrapper.getInstance().checkHexMeetPermission(HexMeet.this)) {
            PermissionWrapper.getInstance().hasFloatWindowPermission(HexMeet.this, true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleWebInvite(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        showUpgradeHint(UpgradeActivity.isShowUpgradeHintOnMe());
        doResumeProcess();
    }

    private void doResumeProcess() {
        ResourceUtils.getInstance().initScreenSize();
        HjtApp.getInstance().checkCallScreen();

        if(HjtApp.getInstance().getAppService().isCalling()) {
            LOG.info("onResume: Resume Call");
            HjtApp.getInstance().stopFloatService();
            Intent intent = new Intent();
            intent.setClass(HexMeet.this, Conversation.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if(contentLp != null) {
            View contentView = findViewById(R.id.content);
            LOG.info("margin: " +contentLp.bottomMargin);
            contentView.setLayoutParams(contentLp);
        }
    }

    @SuppressLint("StringFormatInvalid")
    private boolean failedCheckToken() {
        if (SystemCache.getInstance().getLoginResponse() == null) {

            LOG.info("checkToken: Return to login");
            Utils.showToast(HexMeet.this,
                    String.format(getString(R.string.login_first), getString(R.string.app_name)));
            Intent intent = new Intent(HexMeet.this, Login.class);
            intent.setData(getIntent().getData());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private boolean isBackOncePressed = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            contentLp = (RelativeLayout.LayoutParams) findViewById(R.id.content).getLayoutParams();
            if(conferenceListFrag.isResumed() && conferenceListFrag.onBackClick(contentLp.bottomMargin == 0)) {
                return true;
            }

            if (isBackOncePressed) {
                handler.removeMessages(0);
                isBackOncePressed = false;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                this.startActivity(intent);
                return true;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isBackOncePressed = true;
                    Utils.showToast(HexMeet.this, getString(R.string.again_exit));
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            isBackOncePressed = false;
                        }
                    }, 5000);
                }
            });
            /*isBackOncePressed = true;
            handler.sendEmptyMessageDelayed(0, 2000);*/
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                isBackOncePressed = false;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        showTab(currentTab);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacksAndMessages(null);
        if(HjtApp.getInstance().getAppService() != null) {
            HjtApp.getInstance().getAppService().setUserInLogin(false);
        }

        if (screenReceiverRegistered && screenReceiver != null) {
            unregisterReceiver(screenReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {
        tabLayout = (ViewGroup) findViewById(R.id.tabs_row);
        dividerLine = findViewById(R.id.divider);

        tabConference = (TextView) tabLayout.findViewById(R.id.conference);
        tabDialing = (TextView) tabLayout.findViewById(R.id.dialing);
        tabMe = (TextView) tabLayout.findViewById(R.id.me);

        tabConference.setOnClickListener(this);
        tabDialing.setOnClickListener(this);
        tabMe.setOnClickListener(this);
    }

    public void hideTabs(final boolean hide) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View contentView = findViewById(R.id.content);
                contentLp = (RelativeLayout.LayoutParams) contentView.getLayoutParams();
                contentLp.bottomMargin = hide ? 0 : ScreenUtil.dp_to_px(53)/*tabLayout.getHeight() + dividerLine.getHeight()*/;
                contentView.setLayoutParams(contentLp);
            }
        });
    }

    public void joinMeeting(final String json) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(json)) {
                    Utils.showToast(HexMeet.this, "params error");
                    return;
                }
                try {
                    JsJoinMeeting meeting = JsonUtil.toObject(json, JsJoinMeeting.class);
                    if(!TextUtils.isEmpty(meeting.getNumericId())) {
                        SystemCache.getInstance().setUserMuteVideo(meeting.isCameraStatus());
                        SystemCache.getInstance().setUserMuteMic(meeting.isMicrophoneStatus());
                        dialOut(meeting.getNumericId(), meeting.getPassword());
                    }
                } catch (Exception e) {
                    LOG.error("shareToWechat: "+ e.getMessage(), e);
                    Utils.showToast(HexMeet.this, R.string.share_failed);
                }
            }
        });
    }

    private String permissionHoldCallNumber, permissionHoldCallPassword;
    public  void dialOut(String number, String password) {
        if(PermissionWrapper.getInstance().checkMeetingPermission(HexMeet.this)) {
            if (NetworkUtil.is3GConnected(this)) {
                warning4gConversation(number, password);
            } else {
                callWithNumber(number, password);
            }
        } else {
            permissionHoldCallNumber = number;
            permissionHoldCallPassword = password;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        if(requestCode == PermissionWrapper.REQUEST_PERMISSIONS_HEXMEET) {
            PermissionWrapper.getInstance().hasFloatWindowPermission(HexMeet.this, true);
        }
        int result = PermissionWrapper.getInstance().processRequestPermissionsResult(requestCode, grantResults);
        if(requestCode == PermissionWrapper.REQUEST_PERMISSIONS_MEETING && result == PermissionWrapper.RESULT_PERMISSIONS_PASS) {
            if(!TextUtils.isEmpty(permissionHoldCallNumber)) {
                if(permissionHoldCallPassword == null) {
                    permissionHoldCallPassword = "";
                }
                if (NetworkUtil.is3GConnected(this)) {
                    warning4gConversation(permissionHoldCallNumber, permissionHoldCallPassword);
                } else {
                    callWithNumber(permissionHoldCallNumber, permissionHoldCallPassword);
                }
            }
        } else if (result == PermissionWrapper.RESULT_PERMISSIONS_REJECT) {
            //can not make call as no permission
        }
    }

    private void warning4gConversation(final String number, final String password) {
        final View view = getLayoutInflater().inflate(R.layout.alertdialog_warning_4g, null);
        final AlertDialog dlg = new AlertDialog.Builder(this).setView(view).create();
        dlg.show();
        Button submit = (Button) view.findViewById(R.id.submit);
        Button cancel = (Button) view.findViewById(R.id.cancel);

        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LOG.info("proceed to call under cell network");
                callWithNumber(number, password);
                dlg.dismiss();
            }
        });

        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LOG.info("cancel call since current is cell network");
                dlg.dismiss();
            }
        });
    }

    public void callWithNumber(final String number, final String password) {
        Intent intent = new Intent();
        intent.setClass(HexMeet.this, ConnectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isVideoCall", true);
        bundle.putBoolean("isFromDialing", true);
        bundle.putString("sipNumber", number);
        intent.putExtras(bundle);
        startActivity(intent);
        LOG.info("make a " + " call to " + number + "*" + password);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HjtApp.getInstance().getAppService().makeCall(number, password);
            }
        }, 2000);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conference:
                showTab(HexMeetTab.CONFERENCE);
                updateTip();
                break;

            case R.id.dialing:
                showTab(HexMeetTab.DIALING);
                break;

            case R.id.me:
                showTab(HexMeetTab.ME);
                break;

            default:
                break;
        }
    }

    public void showTab(HexMeetTab tab) {
        clearSelection();
        currentTab = tab;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideAllFragments(transaction);
        switch (tab) {
            case CONFERENCE:
                selectTabLabel(tabConference, R.drawable.tab_conference_selected);
                if (conferenceListFrag == null) {
                    conferenceListFrag = new ConferenceListFrag();
                    transaction.add(R.id.content, conferenceListFrag);
                } else {
                    transaction.show(conferenceListFrag);
                }
                break;

            case DIALING:
                selectTabLabel(tabDialing, R.drawable.tab_dial_selected);
                if (dialingFrag == null) {
                    dialingFrag = new DialingFrag();
                    transaction.add(R.id.content, dialingFrag);
                } else {
                    transaction.show(dialingFrag);
                }
                break;

            case ME:
                selectTabLabel(tabMe, R.drawable.tab_me_selected);
                if (meFrag == null) {
                    meFrag = new MeFrag();
                    transaction.add(R.id.content, meFrag);
                } else {
                    transaction.show(meFrag);
                }
                break;

            default:
        }
        transaction.commitAllowingStateLoss();
    }

    private int tabAvatarSize = ScreenUtil.dp_to_px(26);

    private void selectTabLabel(TextView tab, int avatarId) {
        Drawable drawable = getResources().getDrawable(avatarId);
        drawable.setBounds(0, 0, tabAvatarSize, tabAvatarSize);
        tab.setCompoundDrawables(null, drawable, null, null);
        tab.setTextColor(Color.parseColor("#4381FF"));
    }

    private void unselectTabLabel(TextView tab, int avatarId) {
        Drawable drawable = getResources().getDrawable(avatarId);
        drawable.setBounds(0, 0, tabAvatarSize, tabAvatarSize);
        tab.setCompoundDrawables(null, drawable, null, null);
        tab.setTextColor(Color.parseColor("#313131"));
    }

    private void clearSelection() {
        unselectTabLabel(tabConference, R.drawable.tab_conference_unselected);
        unselectTabLabel(tabDialing, R.drawable.tab_dial_unselected);
        unselectTabLabel(tabMe, R.drawable.tab_me_unselected);
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (conferenceListFrag != null) {
            transaction.hide(conferenceListFrag);
        }
        if (dialingFrag != null) {
            transaction.hide(dialingFrag);
        }
        if (meFrag != null) {
            transaction.hide(meFrag);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (conferenceListFrag == null && fragment instanceof ConferenceListFrag) {
            conferenceListFrag = (ConferenceListFrag) fragment;
        } else if (dialingFrag == null && fragment instanceof DialingFrag) {
            dialingFrag = (DialingFrag) fragment;
        } else if (meFrag == null && fragment instanceof MeFrag) {
            meFrag = (MeFrag) fragment;
        }
    }

    public void updateLoginToken() {
        if(conferenceListFrag != null/* && conferenceListFrag.isResumed()*/) {
            conferenceListFrag.updateTokenForWeb();
        } else {
            LOG.info("conferenceListFrag not resume, so not update token to web view");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerReachableEvent(ServerReachableEvent event) {
        updateTip(SystemCache.getInstance().getRegisterState(), event.isReachable());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegEvent(RegisterState state) {
        updateTip(state, NetworkUtil.isCloudReachable());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginResultEvent event) {
        if (SystemCache.getInstance().isAnonymousMakeCall()) {
            return;
        }

        if(event.getCode() == LoginResultEvent.LOGIN_SUCCESS) {
            updateLoginToken();
        }
    }

    private void handleWebInvite(Intent intent) {
        int type = intent.getIntExtra(AppCons.INTENT_KEY_WEB_INVITE, 0);

        if(type == 0) {
            LOG.info("handleWebInvite Hexmeet is not Invited");
            return;
        }

        JoinMeetingParam param = SystemCache.getInstance().getJoinMeetingParam();

        if(type == AppCons.INTENT_VALUE_WEB_INVITE_DIAOUT) {
            LOG.info("handleWebInvite Hexmeet direct dial out");
            if (!TextUtils.isEmpty(param.getConferenceNumber())) {
                dialOut(param.getConferenceNumber(), param.getPassword());
                SystemCache.getInstance().setJoinMeetingParam(null);
            } else {
                LOG.error("handleWebInvite Hexmeet direct dial out error no confId");
            }
        } else {
            LOG.info("handleWebInvite Hexmeet anonymous dialout");
            AnonymousJoinMeetActivity.gotoAnonymousMeeting(HexMeet.this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFileMessageEven(FileMessageEvent event) {
        if(event.isSuccess() && !TextUtils.isEmpty(event.getFilePath())) {
            if(meFrag != null && meFrag.isResumed()) {
                meFrag.loadAvatar();
            }
            Log.i("CallBack","FileMessageEvent"+event.getFilePath());
        }
    }
}
