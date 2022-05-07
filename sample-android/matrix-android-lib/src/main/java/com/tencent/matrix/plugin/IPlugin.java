

package com.tencent.matrix.plugin;

import android.app.Application;



public interface IPlugin {

    Application getApplication();

    void init(Application application, PluginListener pluginListener);

    void start();

    void stop();

    void destroy();

    String getTag();

    void onForeground(boolean isForeground);
}
