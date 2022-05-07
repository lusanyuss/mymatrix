

package com.tencent.matrix.iocanary.core;

import com.tencent.matrix.iocanary.IOCanaryPlugin;
import com.tencent.matrix.iocanary.config.IOConfig;
import com.tencent.matrix.iocanary.detect.CloseGuardHooker;
import com.tencent.matrix.iocanary.util.IOCanaryUtil;
import com.tencent.matrix.report.Issue;
import com.tencent.matrix.report.IssuePublisher;

import java.util.List;

/**
 * 
 */

public class IOCanaryCore implements OnJniIssuePublishListener, IssuePublisher.OnIssueDetectListener {
    private static final String TAG = "Matrix.IOCanaryCore";

    private final IOConfig                mIOConfig;

    private final IOCanaryPlugin mIoCanaryPlugin;

    private boolean           mIsStart;
    private CloseGuardHooker  mCloseGuardHooker;

    public IOCanaryCore(IOCanaryPlugin ioCanaryPlugin) {
        mIOConfig = ioCanaryPlugin.getConfig();
        mIoCanaryPlugin = ioCanaryPlugin;
    }

    public void start() {
        initDetectorsAndHookers(mIOConfig);
        synchronized (this) {
            mIsStart = true;
        }
    }

    public synchronized boolean isStart() {
        return mIsStart;
    }

    public void stop() {
        synchronized (this) {
            mIsStart = false;
        }

        if (mCloseGuardHooker != null) {
            mCloseGuardHooker.unHook();
        }

        IOCanaryJniBridge.uninstall();
    }

    @Override
    public void onDetectIssue(Issue issue) {
//        MatrixLog.i(TAG, "onDetectIssue:%d", issue.getType());
        mIoCanaryPlugin.onDetectIssue(issue);
    }

    private void initDetectorsAndHookers(IOConfig ioConfig) {
        assert ioConfig != null;

        if (ioConfig.isDetectFileIOInMainThread()
            || ioConfig.isDetectFileIOBufferTooSmall()
            || ioConfig.isDetectFileIORepeatReadSameFile()) {
            IOCanaryJniBridge.install(ioConfig, this);
        }

        //if only detect io closeable leak use CloseGuardHooker is Better
        if (ioConfig.isDetectIOClosableLeak()) {
            mCloseGuardHooker = new CloseGuardHooker(this);
            mCloseGuardHooker.hook();
        }
    }

    @Override
    public void onIssuePublish(List<IOIssue> issues) {
        if (issues == null) {
            return;
        }

        for (int i = 0; i < issues.size(); i++) {
            mIoCanaryPlugin.onDetectIssue(IOCanaryUtil.convertIOIssueToReportIssue(issues.get(i)));
        }
    }
}
