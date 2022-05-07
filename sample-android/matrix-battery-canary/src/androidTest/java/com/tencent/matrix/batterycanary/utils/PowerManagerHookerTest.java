

package com.tencent.matrix.batterycanary.utils;

import android.content.Context;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.WorkSource;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@RunWith(AndroidJUnit4.class)
public class PowerManagerHookerTest {
    static final String TAG = "Matrix.test.MonitorFeatureWakeLockTest";

    Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void shutDown() {
        PowerManagerServiceHooker.release();
    }

    @Ignore
    @Test
    public void testAcquireWakeup() throws Exception {
        final AtomicBoolean hasAcquired = new AtomicBoolean();
        final AtomicBoolean hasRelease = new AtomicBoolean();
        final AtomicReference<PowerManager.WakeLock> wakLockRef = new AtomicReference<>();
        PowerManagerServiceHooker.addListener(new PowerManagerServiceHooker.IListener() {
            @Override
            public void onAcquireWakeLock(IBinder token, int flags, String tag, String packageName, WorkSource workSource, String historyTag) {
                try {
                    Field mToken = PowerManager.WakeLock.class.getDeclaredField("mToken");
                    mToken.setAccessible(true);
                    Assert.assertEquals(mToken.get(wakLockRef.get()), token);
                    Assert.assertEquals(PowerManager.PARTIAL_WAKE_LOCK, flags);
                    Assert.assertEquals(TAG, tag);
                    hasAcquired.set(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onReleaseWakeLock(IBinder token, int flags) {
                try {
                    Field mToken = PowerManager.WakeLock.class.getDeclaredField("mToken");
                    mToken.setAccessible(true);
                    Assert.assertEquals(mToken.get(wakLockRef.get()), token);
                    Assert.assertEquals(0, flags);
                    hasRelease.set(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakLockRef.set(wakeLock);
        Assert.assertNotNull(wakeLock);

        wakeLock.acquire();
        Thread.sleep(100L);
        Assert.assertTrue(hasAcquired.get());
        Assert.assertFalse(hasRelease.get());
        wakeLock.release();
        Thread.sleep(100L);
        Assert.assertTrue(hasAcquired.get());
        Assert.assertTrue(hasRelease.get());
    }

    @Ignore
    @Test
    public void testAcquireWakeupTimeout() throws Exception {
        final AtomicBoolean hasAcquired = new AtomicBoolean();
        final AtomicBoolean hasRelease = new AtomicBoolean();
        final AtomicReference<PowerManager.WakeLock> wakLockRef = new AtomicReference<>();
        PowerManagerServiceHooker.addListener(new PowerManagerServiceHooker.IListener() {
            @Override
            public void onAcquireWakeLock(IBinder token, int flags, String tag, String packageName, WorkSource workSource, String historyTag) {
                try {
                    Field mToken = PowerManager.WakeLock.class.getDeclaredField("mToken");
                    mToken.setAccessible(true);
                    Assert.assertEquals(mToken.get(wakLockRef.get()), token);
                    Assert.assertEquals(PowerManager.PARTIAL_WAKE_LOCK, flags);
                    Assert.assertEquals(TAG, tag);
                    hasAcquired.set(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onReleaseWakeLock(IBinder token, int flags) {
                try {
                    Field mToken = PowerManager.WakeLock.class.getDeclaredField("mToken");
                    mToken.setAccessible(true);
                    Assert.assertEquals(mToken.get(wakLockRef.get()), token);
                    Assert.assertEquals(65536, flags);
                    hasRelease.set(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakLockRef.set(wakeLock);
        Assert.assertNotNull(wakeLock);

        wakeLock.acquire(1000L);
        Thread.sleep(100L);
        Assert.assertTrue(hasAcquired.get());
        Assert.assertFalse(hasRelease.get());
        Thread.sleep(1000L);
        Assert.assertTrue(hasAcquired.get());
        Assert.assertTrue(hasRelease.get());
    }
}
