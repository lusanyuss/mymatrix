

package com.tencent.sqlitelint.util;

import java.lang.reflect.Field;

/**
 * 
 */

public final class SQLite3ProfileHooker {
    private static final String TAG = "SQLiteLint.SQLite3ProfileHooker";

    private static volatile boolean sIsTryHook;

    public static void hook() {
        SLog.i(TAG, "hook sIsTryHook: %b", sIsTryHook);

        nativeStartProfile();

        if (!sIsTryHook) {
            boolean hookRet = doHook();
            SLog.i(TAG, "hook hookRet: %b", hookRet);
            sIsTryHook = true;
        }
    }

    public static void unHook() {
        if (sIsTryHook) {
            boolean unHookRet = doUnHook();
            SLog.i(TAG, "unHook unHookRet: %b", unHookRet);
            sIsTryHook = false;
        }
    }

    private static boolean doHook() {
        if (!hookOpenSQLite3Profile()) {
            SLog.i(TAG, "doHook hookOpenSQLite3Profile failed");
            return false;
        }

        return nativeDoHook();
    }

    private static boolean doUnHook() {
        unHookOpenSQLite3Profile();
        nativeStopProfile();
        return true;
    }

    private static native boolean nativeDoHook();
    private static native void nativeStartProfile();
    private static native void nativeStopProfile();

    private static boolean hookOpenSQLite3Profile() {
        try {
            Class<?> clsSQLiteDebug = Class.forName("android.database.sqlite.SQLiteDebug");

            Field fieldDST = clsSQLiteDebug.getDeclaredField("DEBUG_SQL_TIME");
            fieldDST.setAccessible(true);
            fieldDST.setBoolean(clsSQLiteDebug, true);
            fieldDST.setAccessible(false);

            return true;
        } catch (ClassNotFoundException e) {
            SLog.e(TAG, "prepareHookBeforeOpenDatabase: e=%s", e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            SLog.e(TAG, "prepareHookBeforeOpenDatabase: e=%s", e.getLocalizedMessage());
        } catch (NoSuchFieldException e) {
            SLog.e(TAG, "prepareHookBeforeOpenDatabase: e=%s", e.getLocalizedMessage());
        }

        return false;
    }

    private static boolean unHookOpenSQLite3Profile() {
        try {
            Class<?> clsSQLiteDebug = Class.forName("android.database.sqlite.SQLiteDebug");

            Field fieldDST = clsSQLiteDebug.getDeclaredField("DEBUG_SQL_TIME");
            fieldDST.setAccessible(true);
            fieldDST.setBoolean(clsSQLiteDebug, false);
            fieldDST.setAccessible(false);
            return true;
        } catch (ClassNotFoundException e) {
            SLog.e(TAG, "unHookOpenSQLite3Profile: e=%s", e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            SLog.e(TAG, "unHookOpenSQLite3Profile: e=%s", e.getLocalizedMessage());
        } catch (NoSuchFieldException e) {
            SLog.e(TAG, "unHookOpenSQLite3Profile: e=%s", e.getLocalizedMessage());
        }
        return false;
    }
}
