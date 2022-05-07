

package com.tencent.matrix.backtrace;

import androidx.annotation.Keep;

@Keep
public class WeChatBacktraceNative {

    /**
     * Currently useless.
     *
     * @param packageName
     */
    @Keep
    static native void setPackageName(String packageName);

    /**
     * @param savingPath Where quicken unwind table will be saved.
     */
    @Keep
    static native void setSavingPath(String savingPath);

    /**
     * Notify backtrace native library to acknowledge that we had warmed up.
     * @param hasWarmUp
     */
    @Keep
    static native void setWarmedUp(boolean hasWarmUp);

    /**
     * mode = 0: Fp-based unwind
     * mode = 1: Quicken-based unwind
     * mode = 2: Dwarf-based unwind
     *
     * @param mode to unwind backtrace
     */
    @Keep
    static native void setBacktraceMode(int mode);

    /**
     * @param set quicken always enabled.
     */
    @Keep
    static native void setQuickenAlwaysOn(boolean enable);

    /**
     * Consume all so/oat files that waiting to generate quicken unwind table.
     *
     * @return Array of consumed file paths, end with elf start offset.
     */
    @Keep
    static native String[] consumeRequestedQut();

    /**
     * Warm-up specific so file path.
     *
     * @param soPath
     */
    @Keep
    static native boolean warmUp(String soPath, int elfStartOffset, boolean onlySaveFile);

    /**
     * Notify warmed-up elf file to native library.
     *
     * @param soPath
     * @param elfStartOffset
     */
    @Keep
    static native void notifyWarmedUp(String soPath, int elfStartOffset);

    /**
     * Test loading qut.
     *
     * @param soPath
     * @param elfStartOffset
     */
    @Keep
    static native boolean testLoadQut(String soPath, int elfStartOffset);

    /**
     * Some statistic.
     *
     * @param soPath
     */
    @Keep
    static native int[] statistic(String soPath);

    /**
     * Generate quicken table immediately while stepping stack.
     * @param immediate
     */
    @Keep
    static native void immediateGeneration(boolean immediate);

    /**
     * Enable logger if compilation options defined 'EnableLog'
     */
    @Keep
    static native void enableLogger(boolean enable);

    /**
     * A callback from wechat backtrace native code that will schedule an task
     * to consume QUT generate requests.
     */
    @Keep
    static void requestQutGenerate() { // Be called from native.
//        WeChatBacktrace.instance().requestQutGenerate();
    }
}
