package com.hexmeet.hjt.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hexmeet.hjt.BuildConfig;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.utils.Utils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.log4j.Logger;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private Logger LOG = Logger.getLogger(this.getClass());
    private final long five_minutes = 5 * 60 * 1000l;
    private IWXAPI api;
    public static final String APP_ID = BuildConfig.WX_APPID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onResp(BaseResp resp) {
        String result;
        LOG.info("WX Share resp: ["+resp.errCode+"]");
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = getString(R.string.share_success);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = getString(R.string.share_canceled);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = getString(R.string.auth_error);
                break;
            default:
                result = getString(R.string.unknown_reason_error);
                break;
        }

        Utils.showToast(this, result);

        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }


    @Override
    public void onReq(BaseReq req) {
    }
}
