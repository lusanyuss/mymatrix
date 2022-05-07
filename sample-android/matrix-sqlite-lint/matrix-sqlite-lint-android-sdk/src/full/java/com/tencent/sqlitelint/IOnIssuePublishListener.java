

package com.tencent.sqlitelint;

import java.util.ArrayList;
import java.util.List;

/**
 * When the native checkers find a issue, it will notify the {@link SQLiteLintNativeBridge#onPublishIssue(String, ArrayList)}.
 * And SQLiteLintNativeBridge will trigger this listener
 *
 * 
 */

public interface IOnIssuePublishListener {
    void onPublish(List<SQLiteLintIssue> publishedIssues);
}
