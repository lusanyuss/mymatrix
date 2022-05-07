

#include <QuickenProperties.h>

namespace wechat_backtrace {

    static bool gNativeOnly = false;    // Experimental

    bool QutNativeOnly() {
        return gNativeOnly;
    }

    void SetNativeOnly(const bool native_only) {
        gNativeOnly = native_only;
    }
}