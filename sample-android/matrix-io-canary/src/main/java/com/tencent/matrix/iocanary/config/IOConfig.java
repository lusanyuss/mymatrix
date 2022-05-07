

package com.tencent.matrix.iocanary.config;

import com.tencent.mrs.plugin.IDynamicConfig;
//import com.tencent.matrix.iocanary.detect.CloseGuardHooker;




public final class IOConfig {
    private static final String TAG = "Matrix.IOConfig";

    private static final int DEFAULT_FILE_MAIN_THREAD_TRIGGER_THRESHOLD = 500;
    /**
     * We take 4096B(4KB) as a small size of the buffer
     */
    private static final int DEFAULT_FILE_BUFFER_SMALL_THRESHOLD        = 4096;
    /**
     * If the count of the operation(read/write) with a small buffer, the size of which is smaller than {@link #DEFAULT_FILE_BUFFER_SMALL_THRESHOLD},
     * is over SMALL_BUFFER_OP_TIMES_LIMIT, a issue is published
     */
    private static final int DEFAULT_FILE_BUFFER_SMALL_OP_TIMES         = 20;
    private static final int DEFAULT_FILE_REPEAT_READ_TIMES_THRESHOLD   = 5;

    private static final boolean DEFAULT_DETECT_MIAN_THREAD_FILE_IO     = true;
    private static final boolean DEFAULT_DETECT_SMALL_BUFFER            = true;
    private static final boolean DEFAULT_DETECT_REPEAT_READ_SAME_FILE   = true;
    private static final boolean DEFAULT_DETECT_CLOSABLE_LEAK           = true;
    private static final boolean DETECT_NETWORK_IO_IN_MAIN_THREAD       = true;
    private static final boolean DETECT_CURSOR_LEAK                     = true;

    /**
     * The default, lax policy will enable all available detectors
     */
//    public static final IOConfig DEFAULT = new IOConfig.Builder().build();
    private final IDynamicConfig mDynamicConfig;

    private IOConfig(IDynamicConfig dynamicConfig) {
        this.mDynamicConfig = dynamicConfig;
    }

    //switch
    public boolean isDetectFileIOInMainThread() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_file_io_main_thread_enable.name(), DEFAULT_DETECT_MIAN_THREAD_FILE_IO);
    }

    public boolean isDetectFileIORepeatReadSameFile() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_repeated_read_enable.name(), DEFAULT_DETECT_REPEAT_READ_SAME_FILE);
    }

    public boolean isDetectFileIOBufferTooSmall() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_small_buffer_enable.name(), DEFAULT_DETECT_SMALL_BUFFER);
    }

    public boolean isDetectIOClosableLeak() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_closeable_leak_enable.name(), DEFAULT_DETECT_CLOSABLE_LEAK);
    }


    //value
    public int getFileMainThreadTriggerThreshold() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_main_thread_enable_threshold.name(), DEFAULT_FILE_MAIN_THREAD_TRIGGER_THRESHOLD);
    }

    public int getFileBufferSmallThreshold() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_small_buffer_threshold.name(), DEFAULT_FILE_BUFFER_SMALL_THRESHOLD);
    }

    public int getFilBufferSmallOpTimes() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_small_buffer_operator_times.name(), DEFAULT_FILE_BUFFER_SMALL_OP_TIMES);
    }

    public int getFileRepeatReadThreshold() {
        return mDynamicConfig.get(IDynamicConfig.ExptEnum.clicfg_matrix_io_repeated_read_threshold.name(), DEFAULT_FILE_REPEAT_READ_TIMES_THRESHOLD);
    }


    @Override
    public String toString() {
        return String.format("[IOCanary.IOConfig], main_thread:%b, small_buffer:%b, repeat_read:%b, closeable_leak:%b",
                isDetectFileIOInMainThread(), isDetectFileIOBufferTooSmall(), isDetectFileIORepeatReadSameFile(), isDetectIOClosableLeak());
    }

    public static final class Builder {
        private IDynamicConfig mDynamicConfig;

        public Builder() {
        }


        public IOConfig.Builder dynamicConfig(IDynamicConfig dynamicConfig) {
            this.mDynamicConfig = dynamicConfig;
            return this;
        }

        public IOConfig build() {
            return new IOConfig(mDynamicConfig);
        }
    }
}
