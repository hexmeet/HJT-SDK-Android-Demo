package com.hexmeet.hjt.me;

import android.bluetooth.le.ScanFilter;

/**
 * Created by mwoolley on 20/11/2015.
 */
public class ScanFilterFactory {
    private static ScanFilterFactory instance;

    private ScanFilterFactory() {

    }

    public static synchronized ScanFilterFactory getInstance() {
        if (instance == null) {
            instance = new ScanFilterFactory();
        }
        return instance;
    }

    public ScanFilter getScanFilter() {
        return new ScanFilter.Builder().build();
    }
}
