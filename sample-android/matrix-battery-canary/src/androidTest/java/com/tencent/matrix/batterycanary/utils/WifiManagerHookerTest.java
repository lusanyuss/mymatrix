

package com.tencent.matrix.batterycanary.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tencent.matrix.batterycanary.TestUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;


@RunWith(AndroidJUnit4.class)
public class WifiManagerHookerTest {
    static final String TAG = "Matrix.test.BleManagerHookerTest";

    Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void shutDown() {
    }

    @Test
    public void testScanning() throws Exception {
        if (TestUtils.isAssembleTest()) return;

        final AtomicInteger scanInc = new AtomicInteger();
        final AtomicInteger getScanInc = new AtomicInteger();
        SystemServiceBinderHooker hooker = new SystemServiceBinderHooker(Context.WIFI_SERVICE, "android.net.wifi.IWifiManager", new SystemServiceBinderHooker.HookCallback() {
            @Override
            public void onServiceMethodInvoke(Method method, Object[] args) {
                Assert.assertNotNull(method);
                if ("startScan".equals(method.getName())) {
                    scanInc.incrementAndGet();
                } else if ("getScanResults".equals(method.getName())) {
                    getScanInc.incrementAndGet();
                }
            }

            @Nullable
            @Override
            public Object onServiceMethodIntercept(Object receiver, Method method, Object[] args) throws Throwable {
                return null;
            }
        });

        hooker.doHook();
        Assert.assertEquals(0 ,scanInc.get());
        Assert.assertEquals(0 ,getScanInc.get());

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();

        Assert.assertEquals(1 ,scanInc.get());
        Assert.assertEquals(0 ,getScanInc.get());

        wifiManager.getScanResults();
        Assert.assertEquals(1 ,scanInc.get());
        Assert.assertEquals(1 ,getScanInc.get());

        hooker.doUnHook();
    }

    @Test
    public void testWifiCounting() {
        final AtomicInteger scanInc = new AtomicInteger();
        final AtomicInteger getScanInc = new AtomicInteger();

        WifiManagerServiceHooker.addListener(new WifiManagerServiceHooker.IListener() {
            @Override
            public void onStartScan() {
                scanInc.incrementAndGet();
            }
            @Override
            public void onGetScanResults() {
                getScanInc.incrementAndGet();
            }
        });

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(i, scanInc.get());
            Assert.assertEquals(i, getScanInc.get());

            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();

            Assert.assertEquals(i + 1, scanInc.get());
            Assert.assertEquals(i, getScanInc.get());

            wifiManager.getScanResults();
            Assert.assertEquals(i + 1, scanInc.get());
            Assert.assertEquals(i + 1, getScanInc.get());
        }

        WifiManagerServiceHooker.release();
    }

}

