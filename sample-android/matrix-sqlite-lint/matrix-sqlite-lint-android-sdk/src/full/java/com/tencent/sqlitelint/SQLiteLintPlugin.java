

package com.tencent.sqlitelint;

import android.app.Application;
import android.content.Context;

import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.plugin.PluginListener;
import com.tencent.matrix.report.Issue;
import com.tencent.matrix.util.DeviceUtil;
import com.tencent.sqlitelint.behaviour.report.IssueReportBehaviour;
import com.tencent.sqlitelint.config.SQLiteLintConfig;
import com.tencent.sqlitelint.config.SharePluginInfo;
import com.tencent.sqlitelint.util.SLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * //TEMP
 * 
 */

public class SQLiteLintPlugin extends Plugin {
    private static final String TAG = "Matrix.SQLiteLintPlugin";

    private final SQLiteLintConfig mConfig;
    private Context mContext;

    public SQLiteLintPlugin(SQLiteLintConfig config) {
        mConfig = config;
    }

    @Override
    public void init(Application app, PluginListener listener) {
        super.init(app, listener);
        SQLiteLint.init();
        SQLiteLint.setPackageName(app);
        //TODO UnSupport logic

        mContext = app.getApplicationContext();
    }

    @Override
    public void start() {
        super.start();
        if (!isSupported()) {
            return;
        }

        SQLiteLint.setReportDelegate(new IssueReportBehaviour.IReportDelegate() {
            @Override
            public void report(SQLiteLintIssue issue) {
                if (issue == null) {
                    return;
                }

                reportMatrixIssue(issue);
            }
        });

        List<SQLiteLintConfig.ConcernDb> concernDbList = mConfig.getConcernDbList();
        for (int i = 0; i < concernDbList.size(); i++) {
            SQLiteLintConfig.ConcernDb concernDb = concernDbList.get(i);
            String concernedDbPath = concernDb.getInstallEnv().getConcernedDbPath();
            SQLiteLint.install(mContext, concernDb.getInstallEnv(), concernDb.getOptions());
            SQLiteLint.setWhiteList(concernedDbPath, concernDb.getWhiteListXmlResId());
            SQLiteLint.enableCheckers(concernedDbPath, concernDb.getEnableCheckerList());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (!isSupported()) {
            return;
        }

        List<SQLiteLintConfig.ConcernDb> concernDbList = mConfig.getConcernDbList();
        for (int i = 0; i < concernDbList.size(); i++) {
            SQLiteLintConfig.ConcernDb concernDb = concernDbList.get(i);
            SQLiteLint.uninstall(concernDb.getInstallEnv().getConcernedDbPath());
        }

        SQLiteLint.setReportDelegate(null);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getTag() {
        return SharePluginInfo.TAG_PLUGIN;
    }

    public void notifySqlExecution(String concernedDbPath, String sql, int timeCost) {
        if (!isPluginStarted()) {
            SLog.i(TAG, "notifySqlExecution isPluginStarted not");
            return;
        }

        SQLiteLint.notifySqlExecution(concernedDbPath, sql, timeCost);
    }

    public void addConcernedDB(SQLiteLintConfig.ConcernDb concernDb) {
        if (!isPluginStarted()) {
            SLog.i(TAG, "addConcernedDB isPluginStarted not");
            return;
        }

        if (concernDb == null) {
            return;
        }


        mConfig.addConcernDB(concernDb);

        String concernedDbPath = concernDb.getInstallEnv().getConcernedDbPath();
        SQLiteLint.install(mContext, concernDb.getInstallEnv(), concernDb.getOptions());
        SQLiteLint.setWhiteList(concernedDbPath, concernDb.getWhiteListXmlResId());
        SQLiteLint.enableCheckers(concernedDbPath, concernDb.getEnableCheckerList());
    }

    private void reportMatrixIssue(SQLiteLintIssue lintIssue) {
        SLog.i(TAG, "reportMatrixIssue type:%d, isNew %b", lintIssue.type, lintIssue.isNew);
        if (!lintIssue.isNew){
            return;
        }

        Issue issue = new Issue(lintIssue.type);
        issue.setKey(lintIssue.id);
        JSONObject content = new JSONObject();
        issue.setContent(content);
        try {
//            content = DeviceUtil.getDeviceInfo(content, getApplication());
            content.put(DeviceUtil.DEVICE_MACHINE, DeviceUtil.getLevel(getApplication()));

            content.put(SharePluginInfo.ISSUE_KEY_ID, lintIssue.id);
            content.put(SharePluginInfo.ISSUE_KEY_DB_PATH, lintIssue.dbPath);
            content.put(SharePluginInfo.ISSUE_KEY_LEVEL, lintIssue.level);
            content.put(SharePluginInfo.ISSUE_KEY_SQL, lintIssue.sql);
            content.put(SharePluginInfo.ISSUE_KEY_TABLE, lintIssue.table);
            content.put(SharePluginInfo.ISSUE_KEY_DESC, lintIssue.desc);
            content.put(SharePluginInfo.ISSUE_KEY_DETAIL, lintIssue.detail);
            content.put(SharePluginInfo.ISSUE_KEY_ADVICE, lintIssue.advice);
            content.put(SharePluginInfo.ISSUE_KEY_CREATE_TIME, lintIssue.createTime);
            content.put(SharePluginInfo.ISSUE_KEY_STACK, lintIssue.extInfo);
            content.put(SharePluginInfo.ISSUE_KEY_SQL_TIME_COST, lintIssue.sqlTimeCost);
            content.put(SharePluginInfo.ISSUE_KEY_IS_IN_MAIN_THREAD, lintIssue.isInMainThread);
        } catch (JSONException e) {
            SLog.i(TAG, "reportMatrixIssue e:%s", e.getLocalizedMessage());
        }

        onDetectIssue(issue);
    }
}
