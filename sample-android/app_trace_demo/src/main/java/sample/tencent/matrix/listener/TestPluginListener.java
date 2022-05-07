package sample.tencent.matrix.listener;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.tencent.matrix.plugin.DefaultPluginListener;
import com.tencent.matrix.report.Issue;
import com.tencent.matrix.util.MatrixLog;

import java.lang.ref.SoftReference;

import sample.tencent.matrix.issue.IssueFilter;
import sample.tencent.matrix.issue.IssuesListActivity;
import sample.tencent.matrix.issue.IssuesMap;


public class TestPluginListener extends DefaultPluginListener {

    public static final String TAG = "TestPluginListener";

    public SoftReference<Context> softReference;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public TestPluginListener(Context context) {
        super(context);
        softReference = new SoftReference<>(context);
    }

    @Override
    public void onReportIssue(final Issue issue) {
        super.onReportIssue(issue);
        MatrixLog.e(TAG, issue.toString());

        IssuesMap.put(IssueFilter.getCurrentFilter(), issue);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showToast(issue);
            }
        });

        //        jumpToIssueActivity();
    }

    private void showToast(Issue issue) {

        String message = String.format("Report an issue - [%s].", issue.getTag());
        Context context = softReference.get();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG);
        }
    }

    public void jumpToIssueActivity() {
        Context context = softReference.get();
        Intent intent = new Intent(context, IssuesListActivity.class);

        if (context instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

}
