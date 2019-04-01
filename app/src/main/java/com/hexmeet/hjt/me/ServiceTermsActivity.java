package com.hexmeet.hjt.me;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.R;

public class ServiceTermsActivity extends BaseActivity {
    private WebView webView;
//    private ProgressBar processBar;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ServiceTermsActivity.class);
        context.startActivity(intent);
    }

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

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
//                    processBar.setVisibility(View.GONE);
                } else {
//                    processBar.setVisibility(View.VISIBLE);
//                    processBar.setProgress(newProgress);
                }
            }
        });

        String htmlUrl = "file:///android_asset/license.html";
        webView.loadUrl(htmlUrl);

        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
