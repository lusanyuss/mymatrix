

package com.tencent.matrix.batterycanary.utils;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.tencent.matrix.util.MatrixLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class NotificationManagerServiceHooker {
    private static final String TAG = "Matrix.battery.NotificationHooker";

    public interface IListener {
        @AnyThread void onCreateNotificationChannel(@Nullable Object notificationChannel);
        @AnyThread void onCreateNotification(int id, @Nullable Notification notification);
    }

    private static List<IListener> sListeners = new ArrayList<>();
    private static boolean sTryHook;
    private static SystemServiceBinderHooker.HookCallback sHookCallback = new SystemServiceBinderHooker.HookCallback() {

        @Override
        public void onServiceMethodInvoke(Method method, Object[] args) {
            if ("createNotificationChannels".equals(method.getName())) {
                Object notificationChannel = null;
                if (args != null) {
                    for (Object arg : args) {
                        if (arg == null) {
                            continue;
                        }
                        if (arg.getClass().getName().equals("android.content.pm.ParceledListSlice")) {
                            try {
                                Method getListMethod = arg.getClass().getDeclaredMethod("getList");
                                if (getListMethod != null) {
                                    Object list = getListMethod.invoke(arg);
                                    if (list instanceof Iterable) {
                                        for (Object item : (Iterable) list) {
                                            if (item != null && item.getClass().getName().equals("android.app.NotificationChannel")) {
                                                notificationChannel = item;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                MatrixLog.w(TAG, "try parse args fail: " + e.getMessage());
                            }
                        }
                    }
                }
                dispatchCreateNotificationChannel(notificationChannel);

            } else if ("enqueueNotificationWithTag".equals(method.getName())) {
                int notifyId = -1;
                Notification notification = null;
                for (Object item : args) {
                    if (item instanceof Integer) {
                        if (notifyId == -1) {
                            notifyId = (int) item;
                        }
                        continue;
                    }
                    if (item instanceof Notification) {
                        notification = (Notification) item;
                    }
                }
                dispatchCreateNotification(notifyId, notification);
            }
        }

        @Nullable
        @Override
        public Object onServiceMethodIntercept(Object receiver, Method method, Object[] args) {
            return null;
        }
    };

    private static SystemServiceBinderHooker sHookHelper = new SystemServiceBinderHooker(Context.NOTIFICATION_SERVICE, "android.app.INotificationManager", sHookCallback);

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

    private static void dispatchCreateNotificationChannel(@Nullable Object channel) {
        for (IListener item : sListeners) {
            item.onCreateNotificationChannel(channel);
        }
    }

    private static void dispatchCreateNotification(int id, @Nullable Notification notification) {
        for (IListener item : sListeners) {
            item.onCreateNotification(id, notification);
        }
    }
}
