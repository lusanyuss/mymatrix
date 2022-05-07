

package com.tencent.matrix.resource;

import android.app.Activity;
import android.app.Application;
import android.os.Build;

import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.plugin.PluginListener;
import com.tencent.matrix.resource.config.ResourceConfig;
import com.tencent.matrix.resource.config.SharePluginInfo;
import com.tencent.matrix.resource.processor.BaseLeakProcessor;
import com.tencent.matrix.resource.watcher.ActivityLifeCycleCallbacksAdapter;
import com.tencent.matrix.resource.watcher.ActivityRefWatcher;
import com.tencent.matrix.util.MatrixLog;



public class ResourcePlugin extends Plugin {
    private static final String TAG = "Matrix.ResourcePlugin";

    private final ResourceConfig mConfig;
    private ActivityRefWatcher mWatcher = null;

    public ResourcePlugin(ResourceConfig config) {
        mConfig = config;
    }

    public static void activityLeakFixer(Application application) {
        // Auto break the path from Views in their holder to gc root when activity is destroyed.
        application.registerActivityLifecycleCallbacks(new ActivityLifeCycleCallbacksAdapter() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                ActivityLeakFixer.fixInputMethodManagerLeak(activity);
                ActivityLeakFixer.unbindDrawables(activity);
                ActivityLeakFixer.fixViewLocationHolderLeakApi28(activity);
            }
        });
    }

    public ActivityRefWatcher getWatcher() {
        return mWatcher;
    }

    @Override
    public void init(Application app, PluginListener listener) {
        super.init(app, listener);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            MatrixLog.e(TAG, "API is low Build.VERSION_CODES.ICE_CREAM_SANDWICH(14), ResourcePlugin is not supported");
            unSupportPlugin();
            return;
        }
        mWatcher = new ActivityRefWatcher(app, this);
    }

    @Override
    public void start() {
        super.start();
        if (!isSupported()) {
            MatrixLog.e(TAG, "ResourcePlugin start, ResourcePlugin is not supported, just return");
            return;
        }
        mWatcher.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (!isSupported()) {
            MatrixLog.e(TAG, "ResourcePlugin stop, ResourcePlugin is not supported, just return");
            return;
        }
        mWatcher.stop();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (!isSupported()) {
            MatrixLog.e(TAG, "ResourcePlugin destroy, ResourcePlugin is not supported, just return");
            return;
        }
        mWatcher.destroy();
    }

    @Override
    public String getTag() {
        return SharePluginInfo.TAG_PLUGIN;
    }

    @Override
    public void onForeground(boolean isForeground) {
        MatrixLog.d(TAG, "onForeground: %s", isForeground);
        if (isPluginStarted() && mWatcher != null) {
            mWatcher.onForeground(isForeground);
        }
    }

    public ResourceConfig getConfig() {
        return mConfig;
    }

    public boolean isAnalyzing() {
        return BaseLeakProcessor.isAnalyzing();
    }
}
