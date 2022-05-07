

package com.tencent.sqlitelint;

import android.content.Context;

import com.tencent.sqlitelint.behaviour.BaseBehaviour;
import com.tencent.sqlitelint.util.SLog;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage the SQLiteLintAndroidCores
 * One concerned db one core
 *
 * 
 */

public enum SQLiteLintAndroidCoreManager {
    INSTANCE;

    private static final String TAG = "SQLiteLint.SQLiteLintAndroidCoreManager";
    /**
     * key: the concerned db path
     * value: a SQLiteLintAndroidCore to check this db
     */
    private ConcurrentHashMap<String, SQLiteLintAndroidCore> mCoresMap = new ConcurrentHashMap<>();

    public void install(Context context, SQLiteLint.InstallEnv installEnv, SQLiteLint.Options options) {
        String concernedDbPath = installEnv.getConcernedDbPath();
        if (mCoresMap.containsKey(concernedDbPath)) {
            SLog.w(TAG, "install twice!! ignore");
            return;
        }

        SQLiteLintAndroidCore core = new SQLiteLintAndroidCore(context, installEnv, options);
        mCoresMap.put(concernedDbPath, core);
    }

    public void addBehavior(BaseBehaviour behaviour, String dbPath) {
        if (get(dbPath) == null) {
            return;
        }
        get(dbPath).addBehavior(behaviour);
    }

    public void removeBehavior(BaseBehaviour behaviour, String dbPath) {
        if (get(dbPath) == null) {
            return;
        }
        get(dbPath).removeBehavior(behaviour);
    }

    public SQLiteLintAndroidCore get(String dbPath) {
        return mCoresMap.get(dbPath);
    }

    public void remove(String dbPath) {
        mCoresMap.remove(dbPath);
    }
}
