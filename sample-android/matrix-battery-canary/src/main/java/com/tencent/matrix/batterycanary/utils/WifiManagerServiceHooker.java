

package com.tencent.matrix.batterycanary.utils;

import android.content.Context;
import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.tencent.matrix.util.MatrixLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class WifiManagerServiceHooker {
    private static final String TAG = "Matrix.battery.WifiHooker";

    public interface IListener {
        @AnyThread void onStartScan();
        @AnyThread void onGetScanResults();
    }

    private static List<IListener> sListeners = new ArrayList<>();
    private static boolean sTryHook;
    private static SystemServiceBinderHooker.HookCallback sHookCallback = new SystemServiceBinderHooker.HookCallback() {
        @Override
        public void onServiceMethodInvoke(Method method, Object[] args) {
            if ("startScan".equals(method.getName())) {
                dispatchStartScan();
            } else if ("getScanResults".equals(method.getName())) {
                dispatchGetScanResults();
            }
        }

        @Nullable
        @Override
        public Object onServiceMethodIntercept(Object receiver, Method method, Object[] args) {
            return null;
        }
    };

    private static SystemServiceBinderHooker sHookHelper = new SystemServiceBinderHooker(Context.WIFI_SERVICE, "android.net.wifi.IWifiManager", sHookCallback);

    public synchronized static void addListener(IListener listener) {
        if (listener == null) {
            return;
        }

        if (sListeners.contains(listener)) {
            return;
        }

        sListeners.add(listener);
        checkHook();
    }


    public synchronized static void removeListener(IListener listener) {
        if (listener == null) {
            return;
        }

        sListeners.remove(listener);
        checkUnHook();
    }

    public synchronized static void release() {
        sListeners.clear();
        checkUnHook();
    }

    private static void checkHook() {
        if (sTryHook) {
            return;
        }

        if (sListeners.isEmpty()) {
            return;
        }

        boolean hookRet = sHookHelper.doHook();
        MatrixLog.i(TAG, "checkHook hookRet:%b", hookRet);
        sTryHook = true;
    }

    private static void checkUnHook() {
        if (!sTryHook) {
            return;
        }

        if (!sListeners.isEmpty()) {
            return;
        }

        boolean unHookRet = sHookHelper.doUnHook();
        MatrixLog.i(TAG, "checkUnHook unHookRet:%b", unHookRet);
        sTryHook = false;
    }

    private static void dispatchStartScan() {
        for (IListener item : sListeners) {
            item.onStartScan();
        }
    }

    private static void dispatchGetScanResults() {
        for (IListener item : sListeners) {
            item.onGetScanResults();
        }
    }
}
