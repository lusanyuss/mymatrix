package sample.tencent.matrix.issue;

import com.tencent.matrix.report.Issue;

import org.json.JSONObject;

import java.util.Iterator;

public class ParseIssueUtil {

    public static String parseIssue(Issue issue, boolean onlyShowContent) {

        StringBuilder stringBuilder = new StringBuilder();
        if (!onlyShowContent) {
            stringBuilder.append(Issue.ISSUE_REPORT_TAG).append(" : ").append(issue.getTag()).append("\n");
            stringBuilder.append(Issue.ISSUE_REPORT_TYPE).append(" : ").append(issue.getType()).append("\n");
            stringBuilder.append("key").append(" : ").append(issue.getKey()).append("\n");
        }

        stringBuilder.append("ISSUE Content:").append("\n");

        return pauseJsonObj(stringBuilder, issue.getContent()).toString();

    }

    public static StringBuilder pauseJsonObj(StringBuilder builder, JSONObject object) {
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String val = object.optString(key);
            builder.append("\t\t\t").append(key).append(" : ").append(val).append("\n");
        }

        return builder;
    }

}
