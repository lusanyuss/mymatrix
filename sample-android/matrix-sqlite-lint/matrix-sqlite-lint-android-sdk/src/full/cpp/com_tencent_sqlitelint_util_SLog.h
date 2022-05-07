

//
//

#ifndef SQLITELINT_ANDROID_SLOG_H
#define SQLITELINT_ANDROID_SLOG_H
#include "comm/log/logger.h"
#include <stdio.h>
#include <android/log.h>
#include "loader.h"
#include <assert.h>
#include <stdlib.h>

namespace sqlitelint {
    static const char *const SqliteLintTAG = "SqliteLint.Native";

    #define LOGV(fmt, args...)     SLog(ANDROID_LOG_VERBOSE, (fmt), ##args)
    #define LOGD(fmt, args...)     SLog(ANDROID_LOG_DEBUG, (fmt), ##args)
    #define LOGI(fmt, args...)     SLog(ANDROID_LOG_INFO, (fmt), ##args)
    #define LOGW(fmt, args...)     SLog(ANDROID_LOG_WARN, (fmt), ##args)
    #define LOGE(fmt, args...)     SLog(ANDROID_LOG_ERROR, (fmt), ##args)
}

#endif //SQLITELINT_SQLITELINT_H
