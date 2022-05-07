

package com.tencent.matrix.iocanary.util;

import android.content.Context;

import com.tencent.matrix.iocanary.config.SharePluginInfo;
import com.tencent.matrix.iocanary.core.IOIssue;
import com.tencent.matrix.report.Issue;
//import com.tencent.matrix.util.DeviceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * 
 */

public final class IOCanaryUtil {
    private static final int DEFAULT_MAX_STACK_LAYER = 10;


    private static String sPackageName = null;

    public static void setPackageName(Context context) {
        if (sPackageName == null) {
            sPackageName = context.getPackageName();
        }
    }

    public static String stackTraceToString(final StackTraceElement[] arr) {
        if (arr == null) {
            return "";
        }

        ArrayList<StackTraceElement> stacks = new ArrayList<>(arr.length);
        for (int i = 0; i < arr.length; i++) {
            String className = arr[i].getClassName();
            // remove unused stacks
            if (className.contains("libcore.io")
                || className.contains("com.tencent.matrix.iocanary")
                || className.contains("java.io")
                || className.contains("dalvik.system")
                || className.contains("android.os")) {
                continue;
            }

            stacks.add(arr[i]);
        }
        // stack still too large
        if (stacks.size() > DEFAULT_MAX_STACK_LAYER && sPackageName != null) {
            ListIterator<StackTraceElement> iterator = stacks.listIterator(stacks.size());
            // from backward to forward
            while (iterator.hasPrevious()) {
                StackTraceElement stack = iterator.previous();
                String className = stack.getClassName();
                if (!className.contains(sPackageName)) {
                    iterator.remove();
                }
                if (stacks.size() <= DEFAULT_MAX_STACK_LAYER) {
                    break;
                }
            }
        }
        StringBuffer sb = new StringBuffer(stacks.size());
        for (StackTraceElement stackTraceElement : stacks) {
            sb.append(stackTraceElement).append('\n');
        }
        return sb.toString();
    }

    public static String getThrowableStack(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        return IOCanaryUtil.stackTraceToString(throwable.getStackTrace());
    }


    public static Issue convertIOIssueToReportIssue(IOIssue ioIssue) {
        if (ioIssue == null) {
            return null;
        }

        Issue issue = new Issue(ioIssue.type);
        JSONObject content = new JSONObject();

        try {

//            content = DeviceUtil.getDeviceInfo(content)

            content.put(SharePluginInfo.ISSUE_FILE_PATH, ioIssue.path);
            content.put(SharePluginInfo.ISSUE_FILE_SIZE, ioIssue.fileSize);
            content.put(SharePluginInfo.ISSUE_FILE_OP_TIMES, ioIssue.opCnt);
            content.put(SharePluginInfo.ISSUE_FILE_BUFFER, ioIssue.bufferSize);
            content.put(SharePluginInfo.ISSUE_FILE_COST_TIME, ioIssue.opCostTime);
            content.put(SharePluginInfo.ISSUE_FILE_READ_WRITE_TYPE, ioIssue.opType);
            content.put(SharePluginInfo.ISSUE_FILE_OP_SIZE, ioIssue.opSize);
            content.put(SharePluginInfo.ISSUE_FILE_THREAD, ioIssue.threadName);
            content.put(SharePluginInfo.ISSUE_FILE_STACK, ioIssue.stack);
            content.put(SharePluginInfo.ISSUE_FILE_REPEAT_COUNT, ioIssue.repeatReadCnt);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        issue.setContent(content);
        return issue;
    }
}
