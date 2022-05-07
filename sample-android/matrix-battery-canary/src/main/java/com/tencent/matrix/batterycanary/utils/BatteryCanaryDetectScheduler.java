

package com.tencent.matrix.batterycanary.utils;

import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.RestrictTo;

import com.tencent.matrix.util.MatrixHandlerThread;

/**
 *  Schedule the detect task(runnable) in a single thread and in FIFO.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BatteryCanaryDetectScheduler {
    private static final String TAG = "Matrix.battery.DetectScheduler";

    private Handler mDetectHandler;
    private boolean started = false;

    public BatteryCanaryDetectScheduler() {
    }

    /**
     * Add to the end. Run in the called thread
     *
     * @param detectTask
     */
    public void addDetectTask(Runnable detectTask) {
        mDetectHandler.post(detectTask);
    }

    public void addDetectTask(Runnable detectTask, long delayInMillis) {
        mDetectHandler.postDelayed(detectTask, delayInMillis);
    }

    public void start() {
        if (started) {
            return;
        }
        HandlerThread detectThread = MatrixHandlerThread.getDefaultHandlerThread();
        mDetectHandler = new Handler(detectThread.getLooper());
        started = true;
    }

    public void quit() {
        if (started) {
            mDetectHandler.removeCallbacksAndMessages(null);
            started = false;
        }
    }
}
