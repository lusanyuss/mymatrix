

package com.tencent.matrix.resource.watcher;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;



public class ActivityLifeCycleCallbacksAdapter implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Override it if needed.
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // Override it if needed.
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // Override it if needed.
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // Override it if needed.
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // Override it if needed.
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Override it if needed.
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Override it if needed.
    }
}
