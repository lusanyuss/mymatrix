

package com.tencent.matrix.iocanary.config;



public class SharePluginInfo {
    public static final String TAG_PLUGIN = "io";

    public static final class IssueType {
        public static final int ISSUE_UNKNOWN                       = 0x0;

        /**
         *  enum IssueType {
         *     kIssueMainThreadIO = 1,
         *     kIssueSmallBuffer,
         *     kIssueRepeatRead
         *  };
         */
        public static final int ISSUE_IO_CLOSABLE_LEAK              = 0x4;
        public static final int ISSUE_NETWORK_IO_IN_MAIN_THREAD     = 0x5;
        public static final int ISSUE_IO_CURSOR_LEAK                = 0x6;
    }

    public static final String ISSUE_FILE_PATH            = "path";
    public static final String ISSUE_FILE_SIZE            = "size";
    public static final String ISSUE_FILE_COST_TIME       = "cost";
    public static final String ISSUE_FILE_STACK           = "stack";
    public static final String ISSUE_FILE_OP_TIMES        = "op";
    public static final String ISSUE_FILE_BUFFER          = "buffer";
    public static final String ISSUE_FILE_THREAD          = "thread";
    public static final String ISSUE_FILE_READ_WRITE_TYPE = "opType";
    public static final String ISSUE_FILE_OP_SIZE         = "opSize";

    public static final String ISSUE_FILE_REPEAT_COUNT = "repeat";
}
