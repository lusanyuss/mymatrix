

package com.tencent.matrix.xlog;

import androidx.annotation.Keep;

public class XLogNative {

    public static void setXLogger(String pathOfXlogSo) {
        setXLoggerNative(pathOfXlogSo);
    }

    /**
     * Set XLog so path
     */
    @Keep
    private static native void setXLoggerNative(String pathOfXlogSo);
}
