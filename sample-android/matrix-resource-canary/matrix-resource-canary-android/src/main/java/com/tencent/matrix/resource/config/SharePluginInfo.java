

package com.tencent.matrix.resource.config;



public class SharePluginInfo {
    public static final String TAG_PLUGIN = "memory";

    public static final String ISSUE_RESULT_PATH = "resultZipPath";

    public static final String ISSUE_DUMP_MODE     = "dump_mode";
    public static final String ISSUE_ACTIVITY_NAME = "activity";
    public static final String ISSUE_REF_KEY       = "ref_key";
    public static final String ISSUE_LEAK_DETAIL   = "leak_detail";
    public static final String ISSUE_COST_MILLIS   = "cost_millis";
    public static final String ISSUE_LEAK_PROCESS  = "leak_process";
    public static final String ISSUE_DUMP_DATA     = "dump_data";
    public static final String ISSUE_NOTIFICATION_ID     = "notification_id";

    public static final class IssueType {
        public static final int LEAK_FOUND          = 0;
        public static final int ERR_FILE_NOT_FOUND  = 2;
        public static final int ERR_ANALYSE_OOM     = 3;
        public static final int ERR_UNSUPPORTED_API = 4;
    }
}
