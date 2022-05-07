

package com.tencent.matrix.iocanary.detect;


import android.annotation.SuppressLint;

import com.tencent.matrix.report.IssuePublisher;
import com.tencent.matrix.util.MatrixLog;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This hooker is special and it acts as a detector too. So it's also a IssuePublisher
 * When it get the hook, it get a issue too.
 * Issue is that a closeable leak which we must avoid in our project.
 *
 * 
 */

public final class CloseGuardHooker {
    private static final String TAG = "Matrix.CloseGuardHooker";

    private volatile boolean mIsTryHook;

    private volatile static Object sOriginalReporter;

    private final IssuePublisher.OnIssueDetectListener issueListener;

    public CloseGuardHooker(IssuePublisher.OnIssueDetectListener issueListener) {
        this.issueListener = issueListener;
    }

    /**
     * set to true when a certain thread try hook once; even failed.
     */
    public void hook() {
        MatrixLog.i(TAG, "hook sIsTryHook=%b", mIsTryHook);
        if (!mIsTryHook) {
            boolean hookRet = tryHook();
            MatrixLog.i(TAG, "hook hookRet=%b", hookRet);
            mIsTryHook = true;
        }
    }

    public void unHook() {
        boolean unHookRet = tryUnHook();
        MatrixLog.i(TAG, "unHook unHookRet=%b", unHookRet);
        mIsTryHook = false;
    }

    /**
     * TODO comment
     * Use a way of dynamic proxy to hook
     * <p>
     * warn of sth: detectLeakedClosableObjects may be disabled again after this tryHook once called
     *
     * @return
     */
    private boolean tryHook() {
        try {
            Class<?> closeGuardCls = Class.forName("dalvik.system.CloseGuard");
            Class<?> closeGuardReporterCls = Class.forName("dalvik.system.CloseGuard$Reporter");
            @SuppressLint("SoonBlockedPrivateApi") // FIXME
            Method methodGetReporter = closeGuardCls.getDeclaredMethod("getReporter");
            Method methodSetReporter = closeGuardCls.getDeclaredMethod("setReporter", closeGuardReporterCls);
            Method methodSetEnabled = closeGuardCls.getDeclaredMethod("setEnabled", boolean.class);

            sOriginalReporter = methodGetReporter.invoke(null);

            methodSetEnabled.invoke(null, true);

            // open matrix close guard also
            MatrixCloseGuard.setEnabled(true);

            ClassLoader classLoader = closeGuardReporterCls.getClassLoader();
            if (classLoader == null) {
                return false;
            }

            methodSetReporter.invoke(null, Proxy.newProxyInstance(classLoader,
                new Class<?>[]{closeGuardReporterCls},
                new IOCloseLeakDetector(issueListener, sOriginalReporter)));

            return true;
        } catch (Throwable e) {
            MatrixLog.e(TAG, "tryHook exp=%s", e);
        }

        return false;
    }

    private boolean tryUnHook() {
        try {
            Class<?> closeGuardCls = Class.forName("dalvik.system.CloseGuard");
            Class<?> closeGuardReporterCls = Class.forName("dalvik.system.CloseGuard$Reporter");
            Method methodSetReporter = closeGuardCls.getDeclaredMethod("setReporter", closeGuardReporterCls);
            Method methodSetEnabled = closeGuardCls.getDeclaredMethod("setEnabled", boolean.class);

            methodSetReporter.invoke(null, sOriginalReporter);

            methodSetEnabled.invoke(null, false);
            // close matrix close guard also
            MatrixCloseGuard.setEnabled(false);

            return true;
        } catch (Throwable e) {
            MatrixLog.e(TAG, "tryHook exp=%s", e);
        }

        return false;
    }
}
