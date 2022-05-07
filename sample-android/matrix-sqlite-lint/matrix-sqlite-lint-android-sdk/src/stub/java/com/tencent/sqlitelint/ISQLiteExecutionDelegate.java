

package com.tencent.sqlitelint;

import android.database.Cursor;
import android.database.SQLException;



public interface ISQLiteExecutionDelegate {
    Cursor rawQuery(String selection, String... selectionArgs) throws SQLException;
    void execSQL(String sql) throws SQLException;
}
