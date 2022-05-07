

package com.tencent.sqlitelint.config;


import android.database.sqlite.SQLiteDatabase;

import com.tencent.sqlitelint.SQLiteLint;

import java.util.ArrayList;
import java.util.List;

/**
 * // Temp
 *
 * 
 */

public final class SQLiteLintConfig {

    public SQLiteLintConfig(SQLiteLint.SqlExecutionCallbackMode sqlExecutionCallbackMode) {
        if (sqlExecutionCallbackMode != null) {

        }
    }

    public void addConcernDB(ConcernDb concernDB) {
    }

    public List<ConcernDb> getConcernDbList() {
        return new ArrayList<>();
    }

    public static final class ConcernDb {
        public ConcernDb(SQLiteLint.InstallEnv installEnv, SQLiteLint.Options options) {
            if (installEnv != null && options != null) {

            }
        }

        public ConcernDb(SQLiteDatabase db) {
            if (db != null) {

            }
        }

        public ConcernDb setWhiteListXml(final int xmlResId) {
            return this;
        }

        public SQLiteLint.InstallEnv getInstallEnv() {
            return null;
        }

        public SQLiteLint.Options getOptions() {
            return null;
        }

        public int getWhiteListXmlResId() {
            return -1;
        }

        public ConcernDb enableAllCheckers() {
            return this;
        }

        public ConcernDb enableExplainQueryPlanChecker() {
            return this;
        }

        public ConcernDb enableAvoidSelectAllChecker() {
            return this;
        }

        public ConcernDb enableWithoutRowIdBetterChecker() {
            return this;
        }

        public ConcernDb enableAvoidAutoIncrementChecker() {
            return this;
        }

        public ConcernDb enablePreparedStatementBetterChecker() {
            return this;
        }

        public ConcernDb enableRedundantIndexChecker() {
            return this;
        }

        public List<String> getEnableCheckerList() {
            return new ArrayList<>();
        }

    }
}
