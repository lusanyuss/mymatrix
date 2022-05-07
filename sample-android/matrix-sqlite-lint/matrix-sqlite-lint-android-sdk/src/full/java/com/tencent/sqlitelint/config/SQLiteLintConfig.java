

package com.tencent.sqlitelint.config;


import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.tencent.sqlitelint.SQLiteLint;
import com.tencent.sqlitelint.SimpleSQLiteExecutionDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * // Temp
 *
 * 
 */

public final class SQLiteLintConfig {

    private final List<ConcernDb> sConcernDbList;

    public SQLiteLintConfig(SQLiteLint.SqlExecutionCallbackMode sqlExecutionCallbackMode) {
        SQLiteLint.setSqlExecutionCallbackMode(sqlExecutionCallbackMode);
        sConcernDbList = new ArrayList<>();
    }

    public void addConcernDB(ConcernDb concernDB) {
        if (concernDB == null) {
            return;
        }

        if (concernDB.mInstallEnv == null) {
            return;
        }

        String concernDbPath = concernDB.mInstallEnv.getConcernedDbPath();
        if (TextUtils.isEmpty(concernDbPath)) {
            return;
        }

        for (int i = 0; i < sConcernDbList.size(); i++) {
            if (concernDbPath.equals(concernDB.mInstallEnv.getConcernedDbPath())) {
                return;
            }
        }

        sConcernDbList.add(concernDB);


    }

    public List<ConcernDb> getConcernDbList() {
        return sConcernDbList;
    }

    public static final class ConcernDb {
        private static final String EXPLAIN_QUERY_PLAN_CHECKER_NAME        = "ExplainQueryPlanChecker";
        private static final String AVOID_SELECT_ALL_CHECKER_NAME          = "AvoidSelectAllChecker";
        private static final String WITHOUT_ROWID_BETTER_CHECKER_NAME      = "WithoutRowIdBetterChecker";
        private static final String AVOID_AUTO_INCREMENT_CHECKER_NAME      = "AvoidAutoIncrementChecker";
        private static final String PREPARED_STATEMENT_BETTER_CHECKER_NAME = "PreparedStatementBetterChecker";
        private static final String REDUNDANT_INDEX_CHECKER_NAME           = "RedundantIndexChecker";

        private final SQLiteLint.InstallEnv mInstallEnv;
        private final SQLiteLint.Options    mOptions;
        private       int                   mWhiteListXmlResId;
        private final List<String> mEnableCheckerList = new ArrayList<>();

        public ConcernDb(SQLiteLint.InstallEnv installEnv, SQLiteLint.Options options) {
            mInstallEnv = installEnv;
            mOptions = options;
        }

        public ConcernDb(SQLiteDatabase db) {
            assert db != null;
            mInstallEnv = new SQLiteLint.InstallEnv(db.getPath(), new SimpleSQLiteExecutionDelegate(db));
            mOptions = SQLiteLint.Options.LAX;
        }

        /**
         * <whilte-list>
         * <checker name="ScanTableDetectExplainChecker">
         * <element>select * from sqlite_master where type='table'</element>
         * </checker>
         * <checker name="RedundantIndexTableInfoChecker">
         * </checker>
         * </whilte-list>
         */
        public ConcernDb setWhiteListXml(final int xmlResId) {
            mWhiteListXmlResId = xmlResId;
            return this;
        }

        public SQLiteLint.InstallEnv getInstallEnv() {
            return mInstallEnv;
        }

        public SQLiteLint.Options getOptions() {
            return mOptions;
        }

        public int getWhiteListXmlResId() {
            return mWhiteListXmlResId;
        }

        public ConcernDb enableAllCheckers() {
            return enableExplainQueryPlanChecker()
                .enableAvoidSelectAllChecker()
                .enableWithoutRowIdBetterChecker()
                .enableAvoidAutoIncrementChecker()
                .enablePreparedStatementBetterChecker()
                .enableRedundantIndexChecker();
        }

        public ConcernDb enableExplainQueryPlanChecker() {
            return enableChecker(EXPLAIN_QUERY_PLAN_CHECKER_NAME);
        }

        public ConcernDb enableAvoidSelectAllChecker() {
            return enableChecker(AVOID_SELECT_ALL_CHECKER_NAME);
        }

        public ConcernDb enableWithoutRowIdBetterChecker() {
            return enableChecker(WITHOUT_ROWID_BETTER_CHECKER_NAME);
        }

        public ConcernDb enableAvoidAutoIncrementChecker() {
            return enableChecker(AVOID_AUTO_INCREMENT_CHECKER_NAME);
        }

        public ConcernDb enablePreparedStatementBetterChecker() {
            return enableChecker(PREPARED_STATEMENT_BETTER_CHECKER_NAME);
        }

        public ConcernDb enableRedundantIndexChecker() {
            return enableChecker(REDUNDANT_INDEX_CHECKER_NAME);
        }

        public List<String> getEnableCheckerList() {
            return mEnableCheckerList;
        }

        private ConcernDb enableChecker(String checkerName) {
            mEnableCheckerList.add(checkerName);
            return this;
        }

    }
}
