

#ifndef LIBWECHATBACKTRACE_PROPERTIES_H
#define LIBWECHATBACKTRACE_PROPERTIES_H

#include "BacktraceDefine.h"

namespace wechat_backtrace {

    QUT_EXTERN_C_BLOCK

    bool QutNativeOnly();

    void SetNativeOnly(const bool native_only);

    QUT_EXTERN_C_BLOCK_END
}

#endif //LIBWECHATBACKTRACE_PROPERTIES_H
