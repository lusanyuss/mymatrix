

package com.tencent.matrix.batterycanary.monitor;

import android.app.Application;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.batterycanary.BatteryCanary;
import com.tencent.matrix.batterycanary.BatteryMonitorPlugin;
import com.tencent.matrix.batterycanary.TestUtils;
import com.tencent.matrix.batterycanary.monitor.feature.JiffiesMonitorFeature;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;


@RunWith(AndroidJUnit4.class)
public class BatteryCanaryApiTest {
    static final String TAG = "Matrix.test.BatteryCanaryApiTest";

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
    public void testGetCurrentJiffies() {
        if (TestUtils.isAssembleTest()) {
            return;
        }
        final AtomicReference<JiffiesMonitorFeature.JiffiesSnapshot> ref = new AtomicReference<>(null);

        BatteryCanary.currentJiffies(new BatteryMonitorCore.Callback<JiffiesMonitorFeature.JiffiesSnapshot>() {
            @Override
            public void onGetJiffies(JiffiesMonitorFeature.JiffiesSnapshot snapshot) {
                ref.set(snapshot);
                synchronized (ref) {
                    ref.notifyAll();
                }
            }
        });

        synchronized (ref) {
            try {
                ref.wait(100L);
            } catch (InterruptedException ignored) {
            }
        }

        Assert.assertNull(ref.get());
        BatteryMonitorConfig config = new BatteryMonitorConfig.Builder().enable(JiffiesMonitorFeature.class).build();
        BatteryMonitorCore core = new BatteryMonitorCore(config);
        core.start();
        BatteryMonitorPlugin plugin = new BatteryMonitorPlugin(config);
        Matrix.with().getPlugins().add(plugin);

        BatteryCanary.currentJiffies(new BatteryMonitorCore.Callback<JiffiesMonitorFeature.JiffiesSnapshot>() {
            @Override
            public void onGetJiffies(JiffiesMonitorFeature.JiffiesSnapshot snapshot) {
                ref.set(snapshot);
                synchronized (ref) {
                    ref.notifyAll();
                }
            }
        });

        synchronized (ref) {
            try {
                ref.wait();
            } catch (InterruptedException ignored) {
            }
        }
        Assert.assertNotNull(ref.get());
    }
}
