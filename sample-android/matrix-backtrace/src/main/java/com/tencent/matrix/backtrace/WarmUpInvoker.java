

package com.tencent.matrix.backtrace;

public interface WarmUpInvoker {
    boolean warmUp(String pathOfElf, int offset);
}
