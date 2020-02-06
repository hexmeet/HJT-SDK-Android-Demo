package com.hexmeet.hjt.me;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hexmeet.hjt.AppCons;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.R;

public class ServiceTermsActivity extends BaseActivity {
    private WebView webView;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_terms);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        boolean booleanExtra = getIntent().getBooleanExtra(AppCons.ISTERMSOFSERVICE, false);
        LOG.info("type : "+booleanExtra);
        if(booleanExtra){
            String htmlUrl = "file:///android_asset/license.html";
            webView.loadUrl(htmlUrl);
        }else {
            String privacyUrl = BuildConfig.PRIVACY_URL;
            webView.loadUrl(privacyUrl);
        }

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
