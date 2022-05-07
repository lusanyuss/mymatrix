package sample.tencent.matrix.issue;

import com.tencent.matrix.report.Issue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IssuesMap {

    private static final ConcurrentHashMap<String, List<Issue>> issues = new ConcurrentHashMap<>();
    private static final ArrayList<Issue> allIssues = new ArrayList<>();

    public static void put(@IssueFilter.FILTER String filter, Issue issue) {
        List<Issue> list = issues.get(filter);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(0, issue);
        issues.put(filter, list);

        synchronized (allIssues) {
            allIssues.add(issue);
        }
    }

    public static List<Issue> get(@IssueFilter.FILTER String filter) {
        return issues.get(filter);
    }

    public static int getCount() {
        List list = issues.get(IssueFilter.getCurrentFilter());
        return null == list ? 0 : list.size();
    }

    public static void clear() {
        issues.clear();
    }

    public static Issue getIssueReverse(int index) {
        synchronized (allIssues) {
            if (allIssues.size() <= index) {
                return null;
            }

            return allIssues.get(allIssues.size() - index - 1);
        }
    }

    public static int amountOfIssues() {
        synchronized (allIssues) {
            return allIssues.size();
        }
    }

}
