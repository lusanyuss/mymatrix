

//
//

#ifndef MATRIX_HOOK_LOG_H
#define MATRIX_HOOK_LOG_H

#include <stdio.h>
#include <android/log.h>
#include "Macros.h"

typedef int (*internal_logger_func)(int log_level, const char *tag, const char *format,
                                    va_list varargs);

EXPORT_C void enable_hook_logger(bool enable);
EXPORT_C void internal_hook_logger(int log_level, const char *tag, const char *format, ...);
EXPORT_C void
internal_hook_vlogger(int log_level, const char *tag, const char *format, va_list varargs);

#ifdef EnableLOG

#undef LOGD
#undef LOGI
#undef LOGE

#define LOGD(TAG, FMT, args...) internal_hook_logger(ANDROID_LOG_DEBUG, TAG, FMT, ##args)
#define LOGI(TAG, FMT, args...) internal_hook_logger(ANDROID_LOG_INFO, TAG, FMT, ##args)
#define LOGE(TAG, FMT, args...) internal_hook_logger(ANDROID_LOG_ERROR, TAG, FMT, ##args)

#else
#define LOGD(TAG, FMT, args...)
#define LOGI(TAG, FMT, args...)
#define LOGE(TAG, FMT, args...)

#endif

#ifndef LOG_ALWAYS_FATAL

#define __FAKE_USE_VA_ARGS(...) ((void)(0))
#define __android_second(dummy, second, ...) second
#define __android_rest(first, ...) , ##__VA_ARGS__

#define android_printAssert(cond, tag, ...)                     \
  __android_log_assert(cond, tag,                               \
                       __android_second(0, ##__VA_ARGS__, NULL) \
                           __android_rest(__VA_ARGS__))

#define LOG_ALWAYS_FATAL(LOG_TAG, ...) \
  (((void)android_printAssert(NULL, LOG_TAG, ##__VA_ARGS__)))
#endif

EXPORT_C int flogger0(FILE *fp, const char *fmt, ...);

#endif //MATRIX_HOOK_LOG_H
