

package com.tencent.matrix.batterycanary;

import com.tencent.matrix.batterycanary.utils.BatteryCanaryUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.tencent.matrix.batterycanary.utils.BatteryCanaryUtil.ONE_MIN;
import static org.junit.Assert.assertEquals;


@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JUnit4.class)
public class BatteryCanaryUtilTest {

    @Test
    public void testComputeMinuteAvg() throws Exception {
        Assert.assertEquals(100, BatteryCanaryUtil.computeAvgByMinute(100, 60 * 1000L), 5);
        Assert.assertEquals(200, BatteryCanaryUtil.computeAvgByMinute(100, 30 * 1000L), 5);
        Assert.assertEquals(600, BatteryCanaryUtil.computeAvgByMinute(100, 10 * 1000L), 5);

        for (int i = 0; i < 100; i++) {
            int minute = i + 1;
            for (int j = 1000; j < 100000; j++) {
                Assert.assertEquals(j / minute, BatteryCanaryUtil.computeAvgByMinute(j, ONE_MIN * minute), 5);
            }
        }
    }
}
