

package com.tencent.sqlitelint.behaviour;

import com.tencent.sqlitelint.IOnIssuePublishListener;

/**
 * The behaviour base.
 * A certain subclass defines what to do after finding a sql issue.
 *
 * {@link com.tencent.sqlitelint.SQLiteLintAndroidCore} use the IOnIssuePublishListener as a protocol
 * to dispatch the issue-published event.
 *
 * 
 */

public abstract class BaseBehaviour implements IOnIssuePublishListener {
    protected BaseBehaviour() {
    }
}
