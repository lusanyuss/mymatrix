

package com.tencent.sqlitelint.util;

import android.util.Log;
import com.tencent.matrix.util.MatrixLog;



public class SLog {
    private volatile static SLog mInstance = null;

    public static SLog getInstance() {
        if (mInstance == null) {
            synchronized (SLog.class) {
                if (mInstance == null) {
                    mInstance = new SLog();
                }
            }
        }
        return mInstance;
    }

    public static native void nativeSetLogger(int logLevel);

    public void printLog(int priority, String tag, String msg) {
        switch (priority) {
            case Log.VERBOSE:
                MatrixLog.v(tag, msg);
                return;
            case Log.DEBUG:
                MatrixLog.d(tag, msg);
                return;
            case Log.INFO:
                MatrixLog.i(tag, msg);
                return;
            case Log.WARN:
                MatrixLog.w(tag, msg);
                return;
            case Log.ERROR:
            case Log.ASSERT:
                MatrixLog.e(tag, msg);
                return;
            default:
                MatrixLog.i(tag, msg);
                return;
        }
    }

    public static void e(final String tag, final String format, final Object... args) {
        getInstance().printLog(Log.ERROR, tag, String.format(format, args));
    }

    public static void w(final String tag, final String format, final Object... args) {
        getInstance().printLog(Log.WARN, tag, String.format(format, args));
    }

    public static void i(final String tag, final String format, final Object... args) {
        getInstance().printLog(Log.INFO, tag, String.format(format, args));
    }

    public static void d(final String tag, final String format, final Object... args) {
        getInstance().printLog(Log.DEBUG, tag, String.format(format, args));
    }

    public static void v(final String tag, final String format, final Object... args) {
        getInstance().printLog(Log.VERBOSE, tag, String.format(format, args));
    }

}

