

package com.tencent.matrix.resource;

import android.content.Context;
import android.content.Intent;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.report.Issue;
import com.tencent.matrix.resource.config.SharePluginInfo;
import com.tencent.matrix.util.MatrixLog;

import org.json.JSONObject;

//import com.tencent.matrix.util.DeviceUtil;

/**
 *
 */
public class CanaryResultService extends MatrixJobIntentService {
    private static final String TAG = "Matrix.CanaryResultService";

    private static final int JOB_ID = 0xFAFBFCFE;
    private static final String ACTION_REPORT_HPROF_RESULT = "com.tencent.matrix.resource.result.action.REPORT_HPROF_RESULT";
    private static final String EXTRA_PARAM_RESULT_PATH = "RESULT_PATH";
    private static final String EXTRA_PARAM_ACTIVITY = "RESULT_ACTIVITY";

    public static void reportHprofResult(Context context, String resultPath, String activityName) {
        final Intent intent = new Intent(context, CanaryResultService.class);
        intent.setAction(ACTION_REPORT_HPROF_RESULT);
        intent.putExtra(EXTRA_PARAM_RESULT_PATH, resultPath);
        intent.putExtra(EXTRA_PARAM_ACTIVITY, activityName);
        enqueueWork(context, CanaryResultService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REPORT_HPROF_RESULT.equals(action)) {
                final String resultPath = intent.getStringExtra(EXTRA_PARAM_RESULT_PATH);
                final String activityName = intent.getStringExtra(EXTRA_PARAM_ACTIVITY);

                if (resultPath != null && !resultPath.isEmpty()
                    && activityName != null && !activityName.isEmpty()) {
                    doReportHprofResult(resultPath, activityName);
                } else {
                    MatrixLog.e(TAG, "resultPath or activityName is null or empty, skip reporting.");
                }
            }
        }
    }

    // notice: compatible
    private void doReportHprofResult(String resultPath, String activityName) {
        Issue issue = new Issue(SharePluginInfo.IssueType.LEAK_FOUND);
        final JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(SharePluginInfo.ISSUE_RESULT_PATH, resultPath);
            resultJson.put(SharePluginInfo.ISSUE_ACTIVITY_NAME, activityName);
            issue.setContent(resultJson);
        } catch (Throwable thr) {
            MatrixLog.printErrStackTrace(TAG, thr, "unexpected exception, skip reporting.");
        }

        Plugin plugin =  Matrix.with().getPluginByClass(ResourcePlugin.class);
        if (plugin != null) {
            plugin.onDetectIssue(issue);
        }
    }
}
