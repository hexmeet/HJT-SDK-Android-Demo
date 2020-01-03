package com.hexmeet.hjt.chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.HexMeet;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.contacts.JsP2pMeeting;
import com.hexmeet.hjt.model.RestLoginResp;
import com.hexmeet.hjt.utils.JsonUtil;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.apache.log4j.Logger;

public class ChatGroup extends BaseActivity{
    private Logger LOG = Logger.getLogger(this.getClass());
    private WebView mGroupWeb;
    private LinearLayout mGroupProgressLayout;
    private CircleProgressBar mGroupProgressBar;
    private LinearLayout mGroupNoNetwork;
    private Button mGroupReload;
    private ImageView mGroupCloseWindow;
    private boolean tokenExpired = false;
    private HexMeet hexMeet;
    private boolean isWebLoadComplete = false;
    public final static String SCRIPT_INTERFACE_NAME = "callbackObj";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);
        initView();
        initData();
    }
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    private void initView() {
        mGroupWeb = (WebView) findViewById(R.id.group_web);
        mGroupProgressLayout = (LinearLayout) findViewById(R.id.group_progress_layout);
        mGroupProgressBar = (CircleProgressBar) findViewById(R.id.group_progressBar);
        mGroupNoNetwork = (LinearLayout) findViewById(R.id.group_no_network);
        mGroupReload = (Button) findViewById(R.id.group_reload);
        mGroupCloseWindow = (ImageView) findViewById(R.id.group_close_window);

        mGroupWeb.setHorizontalScrollBarEnabled(false);
        mGroupWeb.setWebContentsDebuggingEnabled(true);
        WebSettings setting = mGroupWeb.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        //setting.setCacheMode(1);
        setting.setAppCacheEnabled(true);
        setting.setAppCachePath(this.getCacheDir().getAbsolutePath());
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        setting.setDomStorageEnabled(true);


        mGroupWeb.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mGroupProgressLayout.setVisibility(View.VISIBLE);
                mGroupProgressBar.setProgress(0);
                isWebLoadComplete = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mGroupProgressLayout.setVisibility(View.GONE);
                isWebLoadComplete = true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                LOG.error("WEB_VIEW_ERROR : " + description + "["+errorCode + "] url: "+failingUrl);
                mGroupNoNetwork.setVisibility(View.VISIBLE);
                mGroupWeb.setVisibility(View.GONE);
                mGroupProgressLayout.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        mGroupWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                LOG.info("newProgress : "+newProgress);
                if (newProgress == 100) {
                    mGroupProgressLayout.setVisibility(View.GONE);
                } else {
                    mGroupProgressBar.setProgress(newProgress);
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

        mGroupWeb.addJavascriptInterface(new JavaScriptInterface() , SCRIPT_INTERFACE_NAME);
        mGroupNoNetwork.findViewById(R.id.group_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConference();
            }
        });

    }

    private void initData() {

    }

    private class JavaScriptInterface {


        @JavascriptInterface
        public void chatContacts(String json){
            LOG.info("JavaScript: chatContacts <"+json+">");
            if(json!=null){
                JsP2pMeeting meeting =JsonUtil.toObject(json, JsP2pMeeting.class);
                LOG.info("JavaScript: chatContacts <"+meeting.toString()+">");
            }
        }

        @JavascriptInterface
        public void closeDialog(){
            LOG.info("JavaScript: closeDialog ");
            onBackPressed();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        LOG.info("onResume()");
        mGroupWeb.onResume();
        mGroupWeb.resumeTimers();
        if(tokenExpired) {
            HjtApp.getInstance().getAppService().loginInLoop(true);
        } else {
            loadConference();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mGroupWeb.onPause();
        mGroupWeb.resumeTimers();
    }

    @Override
    public void onDestroy() {
        if(mGroupWeb!=null) {
            mGroupWeb.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mGroupWeb.clearHistory();
            mGroupWeb.clearCache(true);
            mGroupWeb.removeAllViews();
            mGroupWeb.destroy();
        }
        super.onDestroy();
    }

    private void loadConference() {
        //LOG.error("JavaScript: updateToken error : url not load finished--------------111----------------");
        RestLoginResp restLoginResp = SystemCache.getInstance().getLoginResponse();
        if (restLoginResp != null && NetworkUtil.isNetConnected(this)) {
            mGroupNoNetwork.setVisibility(View.GONE);
            mGroupWeb.setVisibility(View.VISIBLE);
            // webView.clearCache(true);
            StringBuilder sb = new StringBuilder();
             sb.append(restLoginResp.customizedH5UrlPrefix);
            //sb.append(" http://172.20.0.89:3001/#/im_contacts?token=");
            sb.append("/mobile/#/im_contacts?token=");
            sb.append(restLoginResp.getToken());
            if(!TextUtils.isEmpty(restLoginResp.getDoradoVersion())) {
                sb.append("&v="+restLoginResp.getDoradoVersion());
            }
            sb.append("&lang="+ (HjtApp.isCnVersion() ? "cn" : "en"));
            String url = sb.toString();
            LOG.info("Load URL : [" + url + "]");
            Log.i("=========",url);
            mGroupWeb.loadUrl(url);

        } else {
            mGroupNoNetwork.setVisibility(View.VISIBLE);
            mGroupWeb.setVisibility(View.GONE);
        }
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

        mGroupWeb.post(new Runnable() {
            @Override
            public void run() {
                LOG.info("JavaScript: new token ["+token+"]");
                mGroupWeb.evaluateJavascript("javascript:updateToken('" + token + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        LOG.info("JavaScript: new token,return value: "+value);
                    }
                });
                //LOG.error("JavaScript: updateToken error : url not load finished-------------333----------------");
                // loadConference();
            }
        });
    }
}
