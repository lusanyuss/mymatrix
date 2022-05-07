

#ifndef LIBWECHATBACKTRACE_BACKTRACE_H
#define LIBWECHATBACKTRACE_BACKTRACE_H

#include <unwindstack/Maps.h>
#include <unwindstack/Regs.h>
#include <unwindstack/RegsArm64.h>
#include <unwindstack/RegsGetLocal.h>
#include <unwindstack/Unwinder.h>
#include <unwindstack/JitDebug.h>
#include <FpUnwinder.h>
#include <MinimalRegs.h>
#include <QuickenUnwinder.h>
#include <android/log.h>
#include <unwindstack/RegsArm.h>
#include "BacktraceDefine.h"
#include "ElfWrapper.h"

namespace wechat_backtrace {

    QUT_EXTERN_C_BLOCK

    BacktraceMode get_backtrace_mode();

    void set_backtrace_mode(BacktraceMode mode);

    void set_quicken_always_enable(bool enable);

    void BACKTRACE_FUNC_WRAPPER(dwarf_unwind)(
            unwindstack::Regs *regs, std::vector<unwindstack::FrameData> &, size_t);

    void BACKTRACE_FUNC_WRAPPER(fp_unwind)(
            uptr *regs, Frame *frames, const size_t frameMaxSize, size_t &frameSize);

    void BACKTRACE_FUNC_WRAPPER(quicken_unwind)(QuickenContext *context);

    void BACKTRACE_FUNC_WRAPPER(quicken_based_unwind)(
            Frame *frames, const size_t max_frames, size_t &frame_size);

    void BACKTRACE_FUNC_WRAPPER(fp_based_unwind)(
            Frame *frames, const size_t max_frames, size_t &frame_size);

    void BACKTRACE_FUNC_WRAPPER(dwarf_based_unwind)(
            Frame *frames, const size_t max_frames, size_t &frame_size);

    void BACKTRACE_FUNC_WRAPPER(unwind_adapter)(
            Frame *frames, const size_t max_frames, size_t &frame_size);

    void BACKTRACE_FUNC_WRAPPER(restore_frame_detail)(
            const Frame *frames, const size_t frame_size,
            const std::function<void(FrameDetail)> &frame_callback);

    void
    quicken_frame_format(wechat_backtrace::FrameElement &frame_element, size_t num,
                         std::string &data);

    void
    get_stacktrace_elements(wechat_backtrace::Frame *frames, const size_t frame_size,
                            bool shrunk_java_stacktrace,
            /* out */ FrameElement *stacktrace_elements, const size_t max_elements,
            /* out */ size_t &elements_size);

    void BACKTRACE_FUNC_WRAPPER(notify_maps_changed)();

    QUT_EXTERN_C_BLOCK_END
}

#endif //LIBWECHATBACKTRACE_BACKTRACE_H
