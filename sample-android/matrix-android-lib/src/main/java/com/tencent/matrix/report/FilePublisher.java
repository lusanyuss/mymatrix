

package com.tencent.matrix.report;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.matrix.util.MatrixLog;
import com.tencent.matrix.util.MatrixUtil;

import java.util.HashMap;
import java.util.HashSet;


public class FilePublisher extends IssuePublisher {
    private static final String TAG = "Matrix.FilePublisher";

    private final long mExpiredTime;
    private final SharedPreferences.Editor mEditor;
    private final HashMap<String, Long> mPublishedMap;

    private final Context mContext;


    public FilePublisher(Context context, long expire, String tag, OnIssueDetectListener issueDetectListener) {
        super(issueDetectListener);
        this.mContext = context;
        mExpiredTime = expire;
        final String spName = "Matrix_" + tag + MatrixUtil.getProcessName(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        mPublishedMap = new HashMap<>();
        long current = System.currentTimeMillis();
        mEditor = sharedPreferences.edit();
        HashSet<String> spKeys = null;
        if (null != sharedPreferences.getAll()) {
            spKeys = new HashSet<>(sharedPreferences.getAll().keySet());
        }
        if (null != spKeys) {
            for (String key : spKeys) {
                try {
                    long start = sharedPreferences.getLong(key, 0);
                    long costTime = current - start;
                    if (start <= 0 || costTime > mExpiredTime) {
                        mEditor.remove(key);
                    } else {
                        mPublishedMap.put(key, start);
                    }
                } catch (ClassCastException e) {
                    MatrixLog.printErrStackTrace(TAG, e, "might be polluted - sp: %s, key: %s, value : %s", spName, key, sharedPreferences.getAll().get(key));
                }
            }
        }
        if (null != mEditor) {
            mEditor.apply();
        }
    }

    public void markPublished(String key, boolean persist) {
        if (key == null) {
            return;
        }
        if (mPublishedMap.containsKey(key)) {
            return;
        }
        final long now = System.currentTimeMillis();
        mPublishedMap.put(key, now);

        if (persist) {
            SharedPreferences.Editor e = mEditor.putLong(key, now);
            if (null != e) {
                e.apply();
            }
        }
    }

    @Override
    public void markPublished(String key) {
        markPublished(key, true);
    }

    @Override
    public void unMarkPublished(String key) {
        if (key == null) {
            return;
        }
        if (!mPublishedMap.containsKey(key)) {
            return;
        }
        mPublishedMap.remove(key);
        SharedPreferences.Editor e = mEditor.remove(key);
        if (null != e) {
            e.apply();
        }
    }

    @Override
    public boolean isPublished(String key) {
        if (!mPublishedMap.containsKey(key)) {
            return false;
        }
        long start = mPublishedMap.get(key);
        if (start <= 0 || (System.currentTimeMillis() - start) > mExpiredTime) {
            SharedPreferences.Editor e = mEditor.remove(key);
            if (null != e) {
                e.apply();
            }
            mPublishedMap.remove(key);
            return false;
        }
        return true;
    }

    public Context getContext() {
        return mContext;
    }
}
