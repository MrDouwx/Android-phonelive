package com.sevenshuyun.phonelive.utils;

import android.util.Log;

public class Logger {

    private static int level = Log.DEBUG;
    private static boolean debug = true;
    private static boolean debugThis = false;
//    private static boolean debug = false;

    public static void debug(String msg) {
        if (!debug)
            return;
        if (debugThis)
            Log.d("----Logger", msg);
        if (level <= Log.DEBUG)
            Log.d(createTag(), "--"+msg);
    }

    public static void debug(String msg, Throwable tr) {
        if (!debug)
            return;

        if (debugThis)
            Log.d("----Logger", msg, tr);
        if (level <= Log.DEBUG)
            Log.d(createTag(), msg, tr);
    }

    public static void info(String msg) {
        if (!debug)
            return;
        if (level <= Log.INFO) {
            Log.i(createTag(), msg);
        }
    }

    public static void info(String msg, Throwable tr) {
        if (!debug)
            return;
        if (level <= Log.INFO) {
            Log.i(createTag(), msg, tr);
        }
    }

    public static void warn(String msg) {
        if (!debug)
            return;
        if (level <= Log.WARN) {
            Log.w(createTag(), msg);
        }
    }

    public static void warn(String msg, Throwable tr) {
        if (!debug)
            return;
        if (level <= Log.WARN) {
            Log.w(createTag(), msg, tr);
        }
    }

    public static void error(String msg) {
        if (!debug)
            return;
        if (debugThis)
            Log.e("----Logger", msg);
        if (level <= Log.ERROR)
            Log.e(createTag(), msg);
    }

    public static void error(String msg, Throwable tr) {
        if (!debug)
            return;
        if (debugThis)
            Log.e("----Logger", msg, tr);
        if (level <= Log.ERROR)
            Log.e(createTag(), msg, tr);
    }

    private static String createTag() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(Logger.class.getName())) {
                continue;
            }
            return st.getFileName() + ":" + st.getLineNumber()+"----Logger";
        }
        return "";
    }
}
