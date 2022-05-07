

package com.tencent.matrix.trace.listeners;

import androidx.annotation.CallSuper;

import com.tencent.matrix.trace.constants.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 **/
public class IDoFrameListener {

    private Executor executor;
    public long time;
    private int intervalFrame = 0;

    private final static LinkedList<FrameReplay> sPool = new LinkedList<>();

    public static final class FrameReplay {
        public String focusedActivity;
        public long startNs;
        public long endNs;
        public int dropFrame;
        public boolean isVsyncFrame;
        public long intendedFrameTimeNs;
        public long inputCostNs;
        public long animationCostNs;
        public long traversalCostNs;

        public void recycle() {
            if (sPool.size() <= 1000) {
                this.focusedActivity = "";
                this.startNs = 0;
                this.endNs = 0;
                this.dropFrame = 0;
                this.isVsyncFrame = false;
                this.intendedFrameTimeNs = 0;
                this.inputCostNs = 0;
                this.animationCostNs = 0;
                this.traversalCostNs = 0;
                synchronized (sPool) {
                    sPool.add(this);
                }
            }
        }

        public static FrameReplay create() {
            FrameReplay replay;
            synchronized (sPool) {
                replay = sPool.poll();
            }
            if (replay == null) {
                return new FrameReplay();
            }
            return replay;
        }
    }

    private final List<FrameReplay> list = new LinkedList<>();

    public IDoFrameListener() {
        intervalFrame = getIntervalFrameReplay();
    }

    public IDoFrameListener(Executor executor) {
        this.executor = executor;
    }

    @CallSuper
    public void collect(String focusedActivity, long startNs, long endNs, int dropFrame, boolean isVsyncFrame,
                        long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {
        FrameReplay replay = FrameReplay.create();
        replay.focusedActivity = focusedActivity;
        replay.startNs = startNs;
        replay.endNs = endNs;
        replay.dropFrame = dropFrame;
        replay.isVsyncFrame = isVsyncFrame;
        replay.intendedFrameTimeNs = intendedFrameTimeNs;
        replay.inputCostNs = inputCostNs;
        replay.animationCostNs = animationCostNs;
        replay.traversalCostNs = traversalCostNs;
        list.add(replay);
        if (list.size() >= intervalFrame && getExecutor() != null) {
            final List<FrameReplay> copy = new LinkedList<>(list);
            list.clear();
            getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    doReplay(copy);
                    for (FrameReplay record : copy) {
                        record.recycle();
                    }
                }
            });
        }
    }

    @Deprecated
    public void doFrameAsync(String visibleScene, long taskCost, long frameCostMs, int droppedFrames, boolean isVsyncFrame) {

    }

    @Deprecated
    public void doFrameSync(String visibleScene, long taskCost, long frameCostMs, int droppedFrames, boolean isVsyncFrame) {

    }

    @CallSuper
    public void doFrameAsync(String focusedActivity, long startNs, long endNs, int dropFrame, boolean isVsyncFrame,
                             long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {
        long cost = (endNs - intendedFrameTimeNs) / Constants.TIME_MILLIS_TO_NANO;
        doFrameAsync(focusedActivity, cost, cost, dropFrame, isVsyncFrame);
    }

    @CallSuper
    public void doFrameSync(String focusedActivity, long startNs, long endNs, int dropFrame, boolean isVsyncFrame,
                            long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {
        long cost = (endNs - intendedFrameTimeNs) / Constants.TIME_MILLIS_TO_NANO;
        doFrameSync(focusedActivity, cost, cost, dropFrame, isVsyncFrame);
    }

    public void doReplay(List<FrameReplay> list) {

    }


    public Executor getExecutor() {
        return executor;
    }

    public int getIntervalFrameReplay() {
        return 0;
    }


}
