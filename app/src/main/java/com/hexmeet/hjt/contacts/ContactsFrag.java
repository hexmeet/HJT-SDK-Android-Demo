package com.hexmeet.hjt.contacts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hexmeet.hjt.CallState;
import com.hexmeet.hjt.HexMeet;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.call.P2pCallActivity;
import com.hexmeet.hjt.event.CallEvent;
import com.hexmeet.hjt.model.RestLoginResp;
import com.hexmeet.hjt.sdk.Peer;
import com.hexmeet.hjt.utils.JsonUtil;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.hexmeet.hjt.utils.Utils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContactsFrag extends Fragment {

    private Logger LOG = Logger.getLogger(this.getClass());
    private WebView mContactsWeb;
    private LinearLayout mContactsProgressLayout;
    private CircleProgressBar mContactsProgressBar;
    private RelativeLayout mContactsNoNetwork;
    private Button mContactsReload;
    private ImageView mContactsCloseWindow;
    private boolean tokenExpired = false;
    private HexMeet hexMeet;
    private boolean isWebLoadComplete = false;
    public final static String SCRIPT_INTERFACE_NAME = "callbackObj";
    private View root;
    private LinearLayout mWaitingP2p;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.contacts, container, false);
        EventBus.getDefault().register(this);
        initView();
        initData();

        return root;
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    private void initView() {
        mContactsWeb = (WebView) root.findViewById(R.id.contacts_web);
        mContactsProgressLayout = (LinearLayout) root.findViewById(R.id.contacts_progress_layout);
        mContactsProgressBar = (CircleProgressBar) root.findViewById(R.id.contacts_progressBar);
        mContactsNoNetwork = (RelativeLayout) root.findViewById(R.id.contacts_no_network);
        mContactsReload = (Button) root.findViewById(R.id.contacts_reload);
        mContactsCloseWindow = (ImageView) root.findViewById(R.id.contacts_close_window);
        mWaitingP2p = (LinearLayout) root.findViewById(R.id.waiting_p2p);


        mContactsWeb.setHorizontalScrollBarEnabled(false);
        mContactsWeb.setWebContentsDebuggingEnabled(true);
        WebSettings setting = mContactsWeb.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        //setting.setCacheMode(1);
        setting.setAppCacheEnabled(true);
        setting.setAppCachePath(getContext().getCacheDir().getAbsolutePath());
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        setting.setDomStorageEnabled(true);


        mContactsWeb.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mContactsProgressLayout.setVisibility(View.VISIBLE);
                mContactsProgressBar.setProgress(0);
                isWebLoadComplete = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mContactsProgressLayout.setVisibility(View.GONE);
                isWebLoadComplete = true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                LOG.error("WEB_VIEW_ERROR : " + description + "["+errorCode + "] url: "+failingUrl);
                mContactsNoNetwork.setVisibility(View.VISIBLE);
                mContactsWeb.setVisibility(View.GONE);
                mContactsProgressLayout.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        mContactsWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                LOG.info("newProgress : "+newProgress);
                if (newProgress == 100) {
                    mContactsProgressLayout.setVisibility(View.GONE);
                } else {
                    mContactsProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                LOG.warn("webView onJsAlert ["+url+"] : <"+message+">");
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return super.onJsBeforeUnload(view, url, message, result);
            }
        });

        mContactsWeb.addJavascriptInterface(new ContactsJavaScriptInterface() , SCRIPT_INTERFACE_NAME);
        mContactsNoNetwork.findViewById(R.id.contacts_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConference();
            }
        });

    }

    private void initData() {

    }

    private class ContactsJavaScriptInterface {
        @JavascriptInterface
        public void isShowNav(String json){
            LOG.info("JavaScript: isShowNav <"+json+">");
            if(hexMeet != null) {
                hexMeet.hideTabs(json != null && json.equalsIgnoreCase("false"));
            }
        }


        @JavascriptInterface
        public void p2pCall(String json){
            LOG.info("JavaScript: p2pCall <"+json+">");
           /* if(hexMeet != null) {
                hexMeet.showCallIncomingWindow(json);
            }*/
            WaitingP2p(json);

        }

        @JavascriptInterface
        public void joinChat(String json){
            LOG.info("JavaScript: joinWeChat <"+json+">");
            Utils.showToast(getActivity(),getString(R.string.numbered_mode));
            /*if(!TextUtils.isEmpty(json)) {
                JsP2pMeeting meeting = JsonUtil.toObject(json, JsP2pMeeting.class);
                Intent intent = new Intent(getActivity(),ChatContentActivity.class);
                intent.putExtra("username",meeting.getDisplayName());
                intent.putExtra("imageUrl",meeting.getImageUrl());
                intent.putExtra("userId",meeting.getUserId());
                getActivity().startActivity(intent);
            }*/
        }

        @JavascriptInterface
        public void tokenExpired(){
            LOG.info("JavaScript: tokenExpired");
            if(!SystemCache.getInstance().isAnonymousMakeCall() && isResumed()) {
                HjtApp.getInstance().getAppService().loginInLoop(true);
            } else {
                tokenExpired = true;
            }

        }

    }

    private void WaitingP2p(final String json) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(json)) {
                    Utils.showToast(getActivity(), "params error");
                    return;
                }

                mWaitingP2p.setVisibility(View.VISIBLE);
                JsP2pMeeting meeting = JsonUtil.toObject(json, JsP2pMeeting.class);
                LOG.info("meeting : "+meeting.toString());
                boolean videoCall = true;
                if (meeting != null) {
                    // SystemCache.getInstance().setJsP2pMeeting(meeting);
                    String type = meeting.getType();//type 0:音频 1:视频
                    if (type.equals("0")) {
                        mWaitingP2p.setVisibility(View.GONE);
                        Utils.showToast(getActivity(),getString(R.string.numbered_mode));
                        return;
                    }

                    Peer peer = new Peer(Peer.DIRECT_OUT);
                    peer.setNumber(meeting.getUserId());
                    peer.setName(meeting.getDisplayName());
                    peer.setVideoCall(videoCall);
                    peer.setImageUrl(meeting.getImageUrl());
                    peer.setP2P(true);
                    peer.setCalled(false);
                    CallEvent event = new CallEvent(CallState.CONNECTING);
                    event.setPeer(peer);
                    EventBus.getDefault().post(event);


                    HjtApp.getInstance().getAppService().enableVideo(true);
                    HjtApp.getInstance().getAppService().p2pMakeCall(meeting.getUserId(), null, meeting.getDisplayName());
                }
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        hexMeet = (HexMeet) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hexMeet = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        LOG.info("onResume()");
        mContactsWeb.onResume();
        mContactsWeb.resumeTimers();
        if(tokenExpired) {
            HjtApp.getInstance().getAppService().loginInLoop(true);
        } else {
            loadConference();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mContactsWeb.onPause();
        mContactsWeb.resumeTimers();
    }

    @Override
    public void onDestroy() {
        if(mContactsWeb!=null) {
            mContactsWeb.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mContactsWeb.clearHistory();
            mContactsWeb.clearCache(true);
            mContactsWeb.removeAllViews();
            mContactsWeb.destroy();
        }
        super.onDestroy();
    }

    private void loadConference() {
        RestLoginResp restLoginResp = SystemCache.getInstance().getLoginResponse();
        if (restLoginResp != null && NetworkUtil.isNetConnected(getContext())) {
            mContactsNoNetwork.setVisibility(View.GONE);
            mContactsWeb.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
             sb.append(restLoginResp.customizedH5UrlPrefix);
             sb.append("/mobile/#/contacts?token=");
            sb.append(restLoginResp.getToken());
            if(!TextUtils.isEmpty(restLoginResp.getDoradoVersion())) {
                sb.append("&v="+restLoginResp.getDoradoVersion());
            }
            sb.append("&lang="+ (HjtApp.isCnVersion() ? "cn" : "en"));
            String url = sb.toString();
            LOG.info("Load URL : [" + url + "]");
            Log.i("=========",url);
            mContactsWeb.loadUrl(url);

        } else {
            mContactsNoNetwork.setVisibility(View.VISIBLE);
            mContactsWeb.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void updateTokenForWeb() {
        //LOG.error("JavaScript: updateToken error : url not load finished-------------222----------------");
        if(tokenExpired) {
            tokenExpired = false;
            loadConference();
            return;
        }

        tokenExpired = false;
        if(!isWebLoadComplete) {
            LOG.error("JavaScript: updateToken error : url not load finished");
            return;
        }
        updateToken();

    }

    private void updateToken(){
        final String token = SystemCache.getInstance().getLoginResponse().getToken();

        mContactsWeb.post(new Runnable() {
            @Override
            public void run() {
                LOG.info("JavaScript: new token ["+token+"]");
                mContactsWeb.evaluateJavascript("javascript:updateToken('" + token + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        LOG.info("JavaScript: new token,return value: "+value);
                    }
                });
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallStateEvent(CallEvent event) {
        LOG.info("CallEvent : " + event.getCallState());

        if (event.getCallState() == CallState.PEERCONNECTED) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClass(getActivity(), P2pCallActivity.class);
            startActivity(intent);
            mWaitingP2p.setVisibility(View.GONE);
        }

        if (event.getCallState() == CallState.IDLE) {
            if (!TextUtils.isEmpty(event.getEndReason())) {
                Utils.showToast(getActivity(), event.getEndReason());
            }
            mWaitingP2p.setVisibility(View.GONE);
        }
    }

    public boolean onBackClick(boolean isNavBottomBarHide) {
        if(this.mContactsWeb.canGoBack() && isNavBottomBarHide){
            mContactsWeb.evaluateJavascript("javascript:goBack()", null);
            return true;
        } else {
            return false;
        }
    }
}
