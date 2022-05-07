

package com.tencent.sqlitelint.behaviour.report;

import com.tencent.sqlitelint.SQLiteLintIssue;
import com.tencent.sqlitelint.behaviour.BaseBehaviour;

import java.util.List;

/**
 * 
 */

public class IssueReportBehaviour extends BaseBehaviour {

    public interface IReportDelegate {
        void report(SQLiteLintIssue issue);
    }

    private final IReportDelegate mReportDelegate;

    public IssueReportBehaviour(IReportDelegate reportDelegate) {
        mReportDelegate = reportDelegate;
    }

    @Override
    public void onPublish(List<SQLiteLintIssue> publishedIssues) {
        if (mReportDelegate != null) {
            for (int i = 0; i < publishedIssues.size(); i++) {
                mReportDelegate.report(publishedIssues.get(i));
            }
        }
    }
}
