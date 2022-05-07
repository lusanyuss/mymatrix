

package com.tencent.sqlitelint;

import android.app.Application;

import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.plugin.PluginListener;
import com.tencent.sqlitelint.config.SQLiteLintConfig;

/**
 * //TEMP
 * 
 */

public class SQLiteLintPlugin extends Plugin {

    public SQLiteLintPlugin(SQLiteLintConfig config) {
        if (config != null) {
        }
    }

    @Override
    public void init(Application app, PluginListener listener) {
        super.init(app, listener);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getTag() {
        return "SQLiteLint";
    }

    public void notifySqlExecution(String concernedDbPath, String sql, int timeCost) {
    }

    public void addConcernedDB(SQLiteLintConfig.ConcernDb concernDb) {
    }
}
