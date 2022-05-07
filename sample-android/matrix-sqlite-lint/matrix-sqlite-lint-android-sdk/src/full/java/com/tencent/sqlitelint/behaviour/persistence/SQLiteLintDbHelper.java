

package com.tencent.sqlitelint.behaviour.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tencent.sqlitelint.util.SLog;

/**
 * Almost same functionality as {@link SQLiteLintOwnDatabase}, a singleton to get the SQLiteDatabase,
 * but SQLiteLintOwnDatabase can determine the path of the db file
 *
 * 
 */

public enum SQLiteLintDbHelper {
    INSTANCE;

    private static final String TAG = "SQLiteLint.SQLiteLintOwnDatabase";

    private static final String DB_NAME = "SQLiteLintInternal.db";
    private static final int VERSION_1 = 1;

    private volatile InternalDbHelper mHelper;

    public SQLiteDatabase getDatabase() {
        if (mHelper == null) {
            throw new IllegalStateException("getIssueStorage db not ready");
        }

        return mHelper.getWritableDatabase();
    }

    /**
     * initialize in {@link com.tencent.sqlitelint.SQLiteLintAndroidCore}
     * ensures initialized before all the SQLiteLint own db operations begins
     *
     * @param context
     */
    public void initialize(Context context) {
        if (mHelper == null) {
            synchronized (this) {
                if (mHelper == null) {
                    mHelper = new InternalDbHelper(context);
                }
            }
        }
    }

    private static final class InternalDbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "SQLiteLintInternal.db";
        private static final int VERSION_1 = 1;

        InternalDbHelper(Context context) {
            super(context, DB_NAME, null, VERSION_1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            SLog.i(TAG, "onCreate");

            db.execSQL(IssueStorage.DB_VERSION_1_CREATE_SQL);
            for (int i = 0; i < IssueStorage.DB_VERSION_1_CREATE_INDEX.length; i++) {
                db.execSQL(IssueStorage.DB_VERSION_1_CREATE_INDEX[i]);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
