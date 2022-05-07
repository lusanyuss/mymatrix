

package com.tencent.matrix.iocanary.core;



public final class IOIssue {
    public final int type;
    public final String path;
    public final long fileSize;
    public final int opCnt;
    public final long bufferSize;
    public final long opCostTime;
    public final int opType;
    public final long opSize;
    public final String threadName;
    public final String stack;

    public final int repeatReadCnt;

    public IOIssue(int type, String path, long fileSize, int opCnt, long bufferSize, long opCostTime,
                   int opType, long opSize, String threadName, String stack, int repeatReadCnt) {
        this.type = type;
        this.path = path;
        this.fileSize = fileSize;
        this.opCnt = opCnt;
        this.bufferSize = bufferSize;
        this.opCostTime = opCostTime;
        this.opType = opType;
        this.opSize = opSize;
        this.threadName = threadName;
        this.stack = stack;
        this.repeatReadCnt = repeatReadCnt;
    }
}
