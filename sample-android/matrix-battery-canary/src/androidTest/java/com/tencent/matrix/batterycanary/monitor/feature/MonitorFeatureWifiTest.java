

package com.tencent.matrix.batterycanary.monitor.feature;

import android.app.Application;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.batterycanary.monitor.BatteryMonitorConfig;
import com.tencent.matrix.batterycanary.monitor.BatteryMonitorCore;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class MonitorFeatureWifiTest {
    static final String TAG = "Matrix.test.MonitorFeatureWifiTest";

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

    private BatteryMonitorCore mockMonitor() {
        BatteryMonitorConfig config = new BatteryMonitorConfig.Builder()
                .enable(WifiMonitorFeature.class)
                .enableBuiltinForegroundNotify(false)
                .enableForegroundMode(false)
                .wakelockTimeout(1000)
                .greyJiffiesTime(100)
                .foregroundLoopCheckTime(1000)
                .build();
        return new BatteryMonitorCore(config);
    }

    @Test
    public void testScan() throws InterruptedException {
        WifiMonitorFeature feature = new WifiMonitorFeature();
        feature.configure(mockMonitor());

        WifiMonitorFeature.WifiSnapshot snapshot = feature.currentSnapshot();
        Assert.assertEquals(0, (int) snapshot.scanCount.get());
        Assert.assertEquals(0, (int) snapshot.queryCount.get());

        for (int i = 0; i < 50; i++) {
            feature.mTracing.onStartScan();
            snapshot = feature.currentSnapshot();
            Assert.assertEquals(i + 1, (int) snapshot.scanCount.get());
            Assert.assertEquals(0, (int) snapshot.queryCount.get());
        }

        feature.onTurnOff();
        snapshot = feature.currentSnapshot();
        Assert.assertEquals(0, (int) snapshot.scanCount.get());
        Assert.assertEquals(0, (int) snapshot.queryCount.get());
    }

    @Test
    public void getQueryResults() throws InterruptedException {
        WifiMonitorFeature feature = new WifiMonitorFeature();
        feature.configure(mockMonitor());

        WifiMonitorFeature.WifiSnapshot snapshot = feature.currentSnapshot();
        Assert.assertEquals(0, (int) snapshot.scanCount.get());
        Assert.assertEquals(0, (int) snapshot.queryCount.get());

        for (int i = 0; i < 50; i++) {
            feature.mTracing.onGetScanResults();
            snapshot = feature.currentSnapshot();
            Assert.assertEquals(0, (int) snapshot.scanCount.get());
            Assert.assertEquals(i + 1, (int) snapshot.queryCount.get());
        }

        feature.onTurnOff();
        snapshot = feature.currentSnapshot();
        Assert.assertEquals(0, (int) snapshot.scanCount.get());
        Assert.assertEquals(0, (int) snapshot.queryCount.get());
    }
}
