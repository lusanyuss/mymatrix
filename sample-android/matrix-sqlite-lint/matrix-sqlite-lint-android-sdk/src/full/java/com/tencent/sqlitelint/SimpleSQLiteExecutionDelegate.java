

package com.tencent.sqlitelint;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tencent.sqlitelint.util.SLog;

/**
 * When using the android-framework-provided {@link SQLiteDatabase} the manage the SQLite database.
 *
 * We can take this simple implement as the delegate for SQLiteLint's sql execution.
 *
 * 
 */

public final class SimpleSQLiteExecutionDelegate implements ISQLiteExecutionDelegate {
    private static final String TAG = "SQLiteLint.SimpleSQLiteExecutionDelegate";
    private final SQLiteDatabase mDb;

    public SimpleSQLiteExecutionDelegate(SQLiteDatabase db) {
        assert  db != null;

        mDb = db;
    }

    @Override
    public Cursor rawQuery(String selection, String... selectionArgs) throws SQLException {
        if (!mDb.isOpen()) {
            SLog.w(TAG, "rawQuery db close");
            return null;
        }

        return mDb.rawQuery(selection, selectionArgs);
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        if (!mDb.isOpen()) {
            SLog.w(TAG, "rawQuery db close");
            return;
        }

        mDb.execSQL(sql);
    }
}
