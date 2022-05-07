

package com.tencent.matrix.batterycanary.monitor;

import android.app.Application;
import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.batterycanary.BatteryMonitorPlugin;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppStatsTest {
    static final String TAG = "Matrix.test.AppStatsTest";

    Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (!Matrix.isInstalled()) {
            Matrix.init(new Matrix.Builder(((Application) mContext.getApplicationContext())).build());
        }
    }

    @After
    public void shutDown() {
    }

    @Test
    public void testGetCurrAppStats() {
        BatteryMonitorConfig config = new BatteryMonitorConfig.Builder().build();
        BatteryMonitorPlugin plugin = new BatteryMonitorPlugin(config);
        Matrix.with().getPlugins().add(plugin);
        BatteryMonitorCore core = plugin.core();
        core.start();

        AppStats appStats = AppStats.current();
        Assert.assertEquals(0, appStats.getMinute());
        Assert.assertTrue(appStats.isCharging());
        Assert.assertFalse(appStats.isForeground());
        Assert.assertFalse(appStats.hasForegroundService());

        Assert.assertEquals(AppStats.APP_STAT_BACKGROUND, appStats.getAppStat());
        Assert.assertEquals(AppStats.DEV_STAT_CHARGING, appStats.getDevStat());

        core.onForeground(true);
        Assert.assertTrue(appStats.isForeground());
    }
}
