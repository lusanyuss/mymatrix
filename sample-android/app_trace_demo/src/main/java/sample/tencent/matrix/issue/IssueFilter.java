

package sample.tencent.matrix.issue;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class IssueFilter {

    @StringDef({ISSUE_IO, ISSUE_LEAK, ISSUE_TRACE, ISSUE_SQLITELINT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FILTER {
    }

    public static final String ISSUE_IO = "ISSUE_IO";
    public static final String ISSUE_LEAK = "ISSUE_LEAK";
    public static final String ISSUE_TRACE = "ISSUE_TRACE";
    public static final String ISSUE_SQLITELINT = "ISSUE_SQLITELINT";

    @FILTER
    private static String CURRENT_FILTER = ISSUE_IO;


    public static void setCurrentFilter(@FILTER String filter) {
        CURRENT_FILTER = filter;
    }

    @FILTER
    public static String getCurrentFilter() {
        return CURRENT_FILTER;
    }
}
