

//
//
#include <cstdio>
#include "Log.h"

extern "C"
int flogger0(FILE *fp, const char *fmt, ...) {
    if (!fp) {
        return 0;
    }
    va_list args;
    va_start(args, fmt);
    int ret = vfprintf(fp, fmt, args);
    va_end(args);
    return ret;
}

extern "C" internal_logger_func logger_func();

static bool enable_hook_logger_ = false;

extern "C"
void enable_hook_logger(bool enable) {
    enable_hook_logger_ = enable;
}

extern "C" void
internal_hook_logger(int log_level, const char *tag, const char *format, ...) {
    if (!enable_hook_logger_) {
        return;
    }
    va_list ap;
    va_start(ap, format);
    internal_hook_vlogger(log_level, tag, format, ap);
    va_end(ap);
}

extern "C" void
internal_hook_vlogger(int log_level, const char *tag, const char *format, va_list varargs) {
    if (!enable_hook_logger_) {
        return;
    }
    internal_logger_func tmp_logger_func = logger_func();
    if (tmp_logger_func) {
        tmp_logger_func(log_level, tag, format, varargs);
    }
}