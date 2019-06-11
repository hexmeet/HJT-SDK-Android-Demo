package com.hexmeet.hjt.call;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.hexmeet.hjt.HjtApp;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.cache.SystemCache;
import com.hexmeet.hjt.utils.NetworkUtil;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.apache.log4j.Logger;

public class ConfManageWindow {
    private Logger LOG = Logger.getLogger(this.getClass());
    private Conversation conversation;
    private RelativeLayout rootView;
    private Dialog dialog;
    private WebView webView;
    private CircleProgressBar progressBar;
    private View progressLayout;
    private boolean isWebLoadComplete = false;
    private ViewGroup loadFailedInfo;
    public final static String SCRIPT_INTERFACE_NAME = "callbackObj";

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public ConfManageWindow(Conversation activity) {
        initDialog(activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private void initDialog(Conversation activity) {
        this.conversation = activity;
        rootView = (RelativeLayout) LayoutInflater.from(conversation).inflate(R.layout.conference_list, null);
        View closeBtn = rootView.findViewById(R.id.close_window);
        closeBtn.setVisibility(View.VISIBLE);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        initWeb();
        loadFailedInfo = (ViewGroup) rootView.findViewById(R.id.no_network);
        loadFailedInfo.findViewById(R.id.reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConference();
            }
        });

        dialog = new Dialog(conversation, R.style.window_call_manage) {
            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                if (hasFocus) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                }
            }
        };
        hideNavigation();

        dialog.setContentView(rootView);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                webView.onResume();
                webView.resumeTimers();
                loadConference();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                webView.onPause();
                webView.resumeTimers();
                webView.clearHistory();
            }
        });
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    private void initWeb() {
        webView = (WebView) rootView.findViewById(R.id.conference_web);
        progressLayout = rootView.getRootView().findViewById(R.id.progress_layout);
        progressBar = (CircleProgressBar) rootView.findViewById(R.id.progressBar);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
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
                progressBar.setShowProgressText(true);
                isWebLoadComplete = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //progressLayout.setVisibility(View.GONE);
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
                    progressBar.setProgress(99);
                } else {
                    progressBar.setProgress(newProgress);
                }
              // progressBar.setProgress(newProgress);
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
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void show() {
        if (!isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
        }
    }

    public void clean() {
        dismiss();
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webView.destroy();
        dialog = null;
        conversation = null;
    }

    private void loadConference() {
        String token = SystemCache.getInstance().getToken();
        String version = SystemCache.getInstance().getDoradoVersion();
        LOG.info("Load toke : [" + token + "] version : ["+ version + "]");
        if (token != null && NetworkUtil.isNetConnected(conversation)) {
            loadFailedInfo.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);

            StringBuilder sb = new StringBuilder();
            sb.append(SystemCache.getInstance().getLoginResponse().getCustomizedH5UrlPrefix());
            sb.append("/mobile/#/confControl?token=");
            sb.append(token);
            sb.append("&numericId="+ SystemCache.getInstance().getPeer().getNumber());
            sb.append("&lang="+ (HjtApp.isCnVersion() ? "cn" : "en"));
            if(!TextUtils.isEmpty(version)) {
                sb.append("&v="+version);
            }
            String url = sb.toString();
            LOG.info("Load URL : [" + url + "]");
            webView.loadUrl(url);
        } else {
            LOG.info("Load URL : null");
            loadFailedInfo.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }


    private void hideNavigation() {
        View decorView = dialog.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                }
        );
    }

    public class ConferenceJavaScriptInterface {
        @JavascriptInterface
        public void isShowNav(String json){
            LOG.info("JavaScript: isShowNav <"+json+">");
        }

        @JavascriptInterface
        public void shareWechat(String json){
            LOG.info("JavaScript: shareWechat <"+json+">");
            dismiss();
            if(conversation != null) {
                conversation.shareToWechat(json);
            }
        }

        @JavascriptInterface
        public void shareEmail(String json){
            LOG.info("JavaScript: shareEmail <"+json+">");
            if(conversation != null) {
                conversation.shareToEmail(json);
            }
        }

        @JavascriptInterface
        public void joinConf(String json){
            LOG.info("JavaScript: joinConf <"+json+">");
        }

        @JavascriptInterface
        public void tokenExpired(){
            LOG.info("JavaScript: tokenExpired");
            HjtApp.getInstance().getAppService().loginInLoop(true);
        }

        @JavascriptInterface
        public void PageCreated(){
            LOG.info("JavaScript: PageCreated");
            if(conversation != null) {
                conversation.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressLayout.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    public void updateTokenForWeb() {
        if(!isWebLoadComplete) {
            LOG.error("JavaScript: updateToken error : url not load finished");
            return;
        }
        final String token = SystemCache.getInstance().getToken();
        LOG.info("JavaScript: updateToken ["+token+"]");
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("javascript:updateToken("+token+")",null);
                loadConference();
            }
        });

    }

}
