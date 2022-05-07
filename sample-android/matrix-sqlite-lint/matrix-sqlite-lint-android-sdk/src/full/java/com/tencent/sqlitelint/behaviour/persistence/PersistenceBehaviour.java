

package com.tencent.sqlitelint.behaviour.persistence;

import com.tencent.sqlitelint.SQLiteLintIssue;
import com.tencent.sqlitelint.behaviour.BaseBehaviour;

import java.util.List;

/**
 * PersistenceBehaviour is fixed default behavior when issues published
 * And it behaves ahead of all the others.
 * It stores the issues, then the other behaviors can make use of the stored info.
 *
 * For example, alert behaviour can query the stored issues at any time {@link com.tencent.sqlitelint.behaviour.alert.CheckedDatabaseListActivity}.
 * If in need, the report behaviour can use the {@link IssueStorage} to avoid duplicated report
 *
 * 
 */

public class PersistenceBehaviour extends BaseBehaviour {

    @Override
    public void onPublish(List<SQLiteLintIssue> publishedIssues) {
        IssueStorage.saveIssues(publishedIssues);
    }

}
