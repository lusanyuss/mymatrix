

package com.tencent.matrix.iocanary;

import android.app.Application;

import com.tencent.matrix.iocanary.config.IOConfig;
import com.tencent.matrix.iocanary.config.SharePluginInfo;
import com.tencent.matrix.iocanary.core.IOCanaryCore;
import com.tencent.matrix.iocanary.util.IOCanaryUtil;
import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.plugin.PluginListener;

/**
 * Core logic for hookers, detectors and reporter
 * <p>
 * Logic stream like:
 * hooker -> detector -> reporter
 * <p>
 * 
 */

public class IOCanaryPlugin extends Plugin {
    private static final String TAG = "Matrix.IOCanaryPlugin";

    private final IOConfig     mIOConfig;
    private IOCanaryCore mCore;

//    public IOCanaryPlugin() {
//        mIOConfig = IOConfig.DEFAULT;
//    }

    public IOCanaryPlugin(IOConfig ioConfig) {
        mIOConfig = ioConfig;
    }

    @Override
    public void init(Application app, PluginListener listener) {
        super.init(app, listener);
        IOCanaryUtil.setPackageName(app);
        mCore = new IOCanaryCore(this);
    }

    @Override
    public void start() {
        super.start();
        mCore.start();
    }

    @Override
    public void stop() {
        super.stop();
        mCore.stop();
    }

    public IOConfig getConfig() {
        return mIOConfig;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getTag() {
        return SharePluginInfo.TAG_PLUGIN;
    }
}
