

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
public final class LocationManagerServiceHooker {
    private static final String TAG = "Matrix.battery.LocationHooker";

    public interface IListener {
        @AnyThread
        void onRequestLocationUpdates(long minTimeMillis, float minDistance);
    }

    private static List<IListener> sListeners = new ArrayList<>();
    private static boolean sTryHook;
    private static SystemServiceBinderHooker.HookCallback sHookCallback = new SystemServiceBinderHooker.HookCallback() {
        @Override
        public void onServiceMethodInvoke(Method method, Object[] args) {
            if ("requestLocationUpdates".equals(method.getName())) {
                long minTime = -1;
                float minDistance = -1;
                if (args != null) {
                    for (Object item : args) {
                        if (item != null && "android.location.LocationRequest".equals(item.getClass().getName())) {
                            try {
                                Method mFastestInterval = item.getClass().getDeclaredMethod("getFastestInterval");
                                mFastestInterval.setAccessible(true);
                                minTime = (long) mFastestInterval.invoke(item);
                                Method mSmallestDisplacement = item.getClass().getDeclaredMethod("getSmallestDisplacement");
                                mSmallestDisplacement.setAccessible(true);
                                minDistance = (float) mSmallestDisplacement.invoke(item);
                            } catch (Throwable throwable) {
                                MatrixLog.printErrStackTrace(TAG, throwable, "");
                            }
                        }
                    }
                }
                dispatchRequestLocationUpdates(minTime, minDistance);
            }
        }

        @Nullable
        @Override
        public Object onServiceMethodIntercept(Object receiver, Method method, Object[] args) {
            return null;
        }
    };

    private static SystemServiceBinderHooker sHookHelper = new SystemServiceBinderHooker(Context.LOCATION_SERVICE, "android.location.ILocationManager", sHookCallback);

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

    private static void dispatchRequestLocationUpdates(long minTimeMillis, float minDistance) {
        for (IListener item : sListeners) {
            item.onRequestLocationUpdates(minTimeMillis, minDistance);
        }
    }
}
