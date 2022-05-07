

package com.tencent.matrix.report;

import java.util.HashSet;

/**
 * Manage the logic for publish the issue to the listener.
 * Only one listener is accepted.
 */

public class IssuePublisher {

    private final OnIssueDetectListener    mIssueListener;
    private final HashSet<String> mPublishedMap;

    public interface OnIssueDetectListener {
        void onDetectIssue(Issue issue);
    }

    public IssuePublisher(OnIssueDetectListener issueDetectListener) {
        mPublishedMap = new HashSet<>();
        this.mIssueListener = issueDetectListener;
    }

    protected void publishIssue(Issue issue) {
        if (mIssueListener == null) {
            throw new RuntimeException("publish issue, but issue listener is null");
        }
        if (issue != null) {
            mIssueListener.onDetectIssue(issue);
        }
    }

    protected boolean isPublished(String key) {
        if (key == null) {
            return false;
        }

        return mPublishedMap.contains(key);
    }

    protected void markPublished(String key) {
        if (key == null) {
            return;
        }

        mPublishedMap.add(key);
    }

    protected void unMarkPublished(String key) {
        if (key == null) {
            return;
        }
        mPublishedMap.remove(key);
    }
}
