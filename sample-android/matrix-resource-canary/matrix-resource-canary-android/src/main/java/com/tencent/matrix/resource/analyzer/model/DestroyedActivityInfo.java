

package com.tencent.matrix.resource.analyzer.model;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 *
 * If you want to rename this class or any fields of it, please update the new names to
 * field <code>ActivityLeakAnalyzer.DESTROYED_ACTIVITY_INFO_CLASSNAME</code>,
 * <code>ActivityLeakAnalyzer.ACTIVITY_REFERENCE_KEY_FIELDNAME</code>,
 * <code>ActivityLeakAnalyzer.ACTIVITY_REFERENCE_FIELDNAME</code> in analyzer project.
 *
 */

public class DestroyedActivityInfo {
    public final String mKey;
    public final String mActivityName;

    public final WeakReference<Activity> mActivityRef;
    public int mDetectedCount = 0;

    public DestroyedActivityInfo(String key, Activity activity, String activityName) {
        mKey = key;
        mActivityName = activityName;
        mActivityRef = new WeakReference<>(activity);
    }
}
