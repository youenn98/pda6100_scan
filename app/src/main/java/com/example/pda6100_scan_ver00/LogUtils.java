package com.example.pda6100_scan_ver00;

import android.util.Log;

public class LogUtils {
    private static final boolean DEBUG = true;

    public static void v(String className, String content) {
        if (DEBUG) {
            Log.v("Huang, se4710", className + ", " + content);
        }
    }

    public static void d(String className, String content) {
        if (DEBUG) {
            Log.d("Huang, se4710", className + ", " + content);
        }
    }

    public static void i(String className, String content) {
        if (DEBUG) {
            Log.i("Huang, se4710", className + ", " + content);
        }
    }

    public static void w(String className, String content) {
        if (DEBUG) {
            Log.w("Huang, se4710", className + ", " + content);
        }
    }

    public static void e(String className, String content) {
        if (DEBUG) {
            Log.e("Huang", className + ", " + content);
        }
    }
}
