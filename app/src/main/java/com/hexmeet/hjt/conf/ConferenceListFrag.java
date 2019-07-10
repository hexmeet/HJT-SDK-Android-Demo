package com.hexmeet.hjt.conf;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hexmeet.hjt.HexMeet;
import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.model.RestLoginResp;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.apache.log4j.Logger;


public class ConferenceListFrag extends Fragment {
    private Logger LOG = Logger.getLogger(this.getClass());
    public final static String SCRIPT_INTERFACE_NAME = "callbackObj";

    private WebView webView;
    private CircleProgressBar progressBar;
    private View progressLayout;
    private HexMeet hexMeet;
    private boolean isWebLoadComplete = false;
    private ViewGroup loadFailedInfo;
    private boolean tokenExpired = false;

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.conference_list, container, false);

        progressLayout = root.getRootView().findViewById(R.id.progress_layout);
        progressBar = (CircleProgressBar) root.getRootView().findViewById(R.id.progressBar);
        loadFailedInfo = (ViewGroup) root.getRootView().findViewById(R.id.no_network);
        webView = (WebView) root.getRootView().findViewById(R.id.conference_web);
        //webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebContentsDebuggingEnabled(true);
        WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        //setting.setCacheMode(1);
        setting.setAppCacheEnabled(true);
        setting.setAppCachePath(getContext().getCacheDir().getAbsolutePath());
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        setting.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                isWebLoadComplete = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressLayout.setVisibility(View.GONE);
                isWebLoadComplete = true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                LOG.error("WEB_VIEW_ERROR : " + description + "["+errorCode + "] url: "+failingUrl);
                loadFailedInfo.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                LOG.info("newProgress : "+newProgress);
                if (newProgress == 100) {
                    progressLayout.setVisibility(View.GONE);
                } else {
                    progressBar.setProgress(newProgress);
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

        webView.addJavascriptInterface(new ConferenceJavaScriptInterface() , SCRIPT_INTERFACE_NAME);
        loadFailedInfo.findViewById(R.id.reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConference();
            }
        });
        return root;
    }

    /**
     * ConferenceJavaScriptInterface 中的方法必须Public 否则代码混淆后此方法不能被JS调用
     */
    public class ConferenceJavaScriptInterface {
        @JavascriptInterface
        public void isShowNav(String json){
            LOG.info("JavaScript: isShowNav <"+json+">");
            if(hexMeet != null) {
                hexMeet.hideTabs(json != null && json.equalsIgnoreCase("false"));
            }
        }

        @JavascriptInterface
        public void shareWechat(String json){
            //shareWechat <{"name":"中创专用","numericId":"13500135000","password":"112233","domain":null,"softEndpointJoinUrl":"","hardEndpointJoinUrl":"13500135000*112233","h323EndpointJoinUrl":"172.24.0.68##13500135000##112233","sipEndpointJoinUrl":"13500135000*112233@172.24.0.68"}>
            LOG.info("JavaScript: shareWechat <"+json+">");
            if(hexMeet != null) {
                hexMeet.shareToWechat(json);
            }
        }

        @JavascriptInterface
        public void shareEmail(String json){
            //shareEmail <"mailto:?subject=Scott专用&body=会议名称：Scott专用</br>会议时间：2018-07-24 10:58 - 2018-07-27 10:58</br>会议号码：13910001000</br>会议密码: 无</br>会议备注：无</br>H.323终端入会：通过遥控器输入 172.24.0.68##13910001000</br>SIP终端入会：通过遥控器输入 13910001000@172.24.0.68">
            LOG.info("JavaScript: shareEmail <"+json+">");
            if(hexMeet != null) {
                hexMeet.shareToEmail(json);
            }
        }

        @JavascriptInterface
        public void joinConf(String json){
            //joinConf <{"confNumber":"13500135000","password":"112233","cameraStatus":false,"microphoneStatus":false}>
            LOG.info("JavaScript: joinConf <"+json+">");
            if(hexMeet != null) {
                hexMeet.joinMeeting(json);
            }
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

        @JavascriptInterface
        public void doradoVersionUpdate(){
            LOG.info("JavaScript: doradoVersionUpdate");
            loadConference();
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
        final String token = SystemCache.getInstance().getLoginResponse().getToken();
        LOG.info("JavaScript: updateToken ["+token+"]");
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("javascript:updateToken("+token+")", null);
                //LOG.error("JavaScript: updateToken error : url not load finished-------------333----------------");
                loadConference();
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
        webView.onResume();
        webView.resumeTimers();
        if(tokenExpired) {
            HjtApp.getInstance().getAppService().loginInLoop(true);
            //LOG.error("JavaScript: updateToken error : url not load finished-------------444----------------");
        } else {
            loadConference();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
        webView.resumeTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webView.clearHistory();
        webView.destroy();
    }

    private void loadConference() {
        //LOG.error("JavaScript: updateToken error : url not load finished--------------111----------------");
        RestLoginResp restLoginResp = SystemCache.getInstance().getLoginResponse();
        if (restLoginResp != null && NetworkUtil.isNetConnected(getContext())) {
            loadFailedInfo.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
           // webView.clearCache(true);
            StringBuilder sb = new StringBuilder();
            sb.append(restLoginResp.customizedH5UrlPrefix);
            sb.append("/mobile/#/conferences?token=");
            sb.append(restLoginResp.getToken());
           if(!TextUtils.isEmpty(restLoginResp.getDoradoVersion())) {
                sb.append("&v="+restLoginResp.getDoradoVersion());
            }
            sb.append("&lang="+ (HjtApp.isCnVersion() ? "cn" : "en"));
            String url = sb.toString();
            LOG.info("Load URL : [" + url + "]");
            Log.i("=========",url);
            webView.loadUrl(url);

        } else {
            loadFailedInfo.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    public boolean onBackClick(boolean isNavBottomBarHide) {
        if(this.webView.canGoBack() && isNavBottomBarHide){
            this.webView.goBack();
            return true;
        } else {
            return false;
        }
    }

}