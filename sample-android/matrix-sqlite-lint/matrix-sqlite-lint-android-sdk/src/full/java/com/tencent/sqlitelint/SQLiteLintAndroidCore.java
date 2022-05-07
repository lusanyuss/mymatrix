

package com.tencent.sqlitelint;

import android.content.Context;

import com.tencent.matrix.util.MatrixUtil;
import com.tencent.sqlitelint.behaviour.BaseBehaviour;
import com.tencent.sqlitelint.behaviour.alert.IssueAlertBehaviour;
import com.tencent.sqlitelint.behaviour.persistence.IssueStorage;
import com.tencent.sqlitelint.behaviour.persistence.PersistenceBehaviour;
import com.tencent.sqlitelint.behaviour.persistence.SQLiteLintDbHelper;
import com.tencent.sqlitelint.behaviour.report.IssueReportBehaviour;
import com.tencent.sqlitelint.util.SQLite3ProfileHooker;
import com.tencent.sqlitelint.util.SQLiteLintUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The core logic of Android SDK for SQLiteLint
 * 1. Manage the logic stream: Collect executed sql -> Notify the native -> Get checked issue and behave
 * 2. Manage the white list //TODO confim put here?
 *
 * //TODO hide the SQLiteLintAndroidCore from SQLiteLint
 *
 * 
 */
/*package*/class SQLiteLintAndroidCore implements IOnIssuePublishListener {
    private static final String TAG = "SQLiteLint.SQLiteLintAndroidCore";

    private final Context mContext;
    private final String mConcernedDbPath;
    private final ISQLiteExecutionDelegate mSQLiteExecutionDelegate;
    private final List<BaseBehaviour> mBehaviors;

    /**
     * New instance in {@link SQLiteLintAndroidCoreManager#install(Context, SQLiteLint.InstallEnv, SQLiteLint.Options)}
     * A installation in this constructor
     */
    SQLiteLintAndroidCore(Context context, SQLiteLint.InstallEnv installEnv, SQLiteLint.Options options) {
        mContext = context;
        SQLiteLintDbHelper.INSTANCE.initialize(context);
        mConcernedDbPath = installEnv.getConcernedDbPath();
        mSQLiteExecutionDelegate = installEnv.getSQLiteExecutionDelegate();

        if (SQLiteLint.getSqlExecutionCallbackMode() == SQLiteLint.SqlExecutionCallbackMode.HOOK) {
            SQLite3ProfileHooker.hook();
        }

        SQLiteLintNativeBridge.nativeInstall(mConcernedDbPath);

        mBehaviors = new ArrayList<>();
        /*PersistenceBehaviour is a default pre-behaviour */
        mBehaviors.add(new PersistenceBehaviour());
        if (options.isAlertBehaviourEnable()) {
            mBehaviors.add(new IssueAlertBehaviour(context, mConcernedDbPath));
        }
        if (options.isReportBehaviourEnable()) {
            mBehaviors.add(new IssueReportBehaviour(SQLiteLint.sReportDelegate));
        }
    }

    public void addBehavior(BaseBehaviour behaviour) {
        if (!mBehaviors.contains(behaviour)) {
            mBehaviors.add(behaviour);
        }
    }

    public void removeBehavior(BaseBehaviour behaviour) {
        mBehaviors.remove(behaviour);
    }

    //TODO unhook
    public void release() {
        if (SQLiteLint.getSqlExecutionCallbackMode() == SQLiteLint.SqlExecutionCallbackMode.HOOK) {
            SQLite3ProfileHooker.unHook();
        }
        SQLiteLintNativeBridge.nativeUninstall(mConcernedDbPath);
    }

    public ISQLiteExecutionDelegate getSQLiteExecutionDelegate() {
        return mSQLiteExecutionDelegate;
    }

    /**
     * Exposed for {@link SQLiteLint#notifySqlExecution(String, String, int)}
     */
    public void notifySqlExecution(String dbPath, String sql, long timeCost) {
        String extInfoStack = "null";   //get stack only when cost > 8
        if (timeCost >= 8) {
            extInfoStack = SQLiteLintUtil.getThrowableStack(new Throwable());
        }
        SQLiteLintNativeBridge.nativeNotifySqlExecute(dbPath, sql, timeCost, extInfoStack);
    }

    public void setWhiteList(final int xmlResId) {
        CheckerWhiteListLogic.setWhiteList(mContext, mConcernedDbPath, xmlResId);
    }

    public void enableCheckers(List<String> enableCheckers) {
        String[] enableCheckerArr = new String[enableCheckers.size()];
        for (int i = 0; i < enableCheckers.size(); i++) {
            enableCheckerArr[i] = enableCheckers.get(i);
        }
        SQLiteLintNativeBridge.nativeEnableCheckers(mConcernedDbPath, enableCheckerArr);
    }

    @Override
    public void onPublish(List<SQLiteLintIssue> publishedIssues) {
        for (SQLiteLintIssue issue : publishedIssues){
            if (IssueStorage.hasIssue(issue.id)) {
                issue.isNew = false;
            }else {
                issue.isNew = true;
            }
        }
        for (int i = 0; i < mBehaviors.size(); i++) {
            mBehaviors.get(i).onPublish(publishedIssues);
        }
    }
}
