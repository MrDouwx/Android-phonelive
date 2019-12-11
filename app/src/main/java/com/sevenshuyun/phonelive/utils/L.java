package com.sevenshuyun.phonelive.utils;

import android.util.Log;

import com.sevenshuyun.phonelive.AppContext;

/**
 * Created by cxf on 2017/8/3.
 */

public class L {
    private final static String TAG = "log--->";

    public static void e(String s) {
        e(TAG, s);
    }

    public static void e(String tag, String s) {
        if (AppContext.sDeBug) {
            Log.e(tag, s);
        }
    }
}
