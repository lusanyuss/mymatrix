

package com.tencent.sqlitelint;

/**
 * Sqls to be checked are collected through this callback.
 * In a hook way or user manually notify the sql's execution
 *
 * @see SQLiteLint.SqlExecutionCallbackMode
 *
 * 
 */

public interface IOnSqlExecutionCallback {
    void onSqlExecuted(String dbPath, String sql, long timeCost);
}
