

//
//

#ifndef LIBMATRIX_JNI_MACROS_H
#define LIBMATRIX_JNI_MACROS_H

#include <android/log.h>
#define HOOK_LOG_ERROR(fmt, ...) //__android_log_print(ANDROID_LOG_ERROR,  "TestHook", fmt, ##__VA_ARGS__)

#define USE_CRITICAL_CHECK true
#define USE_ALLOC_COUNTER true

/* For testing */
#define ENABLE_FAKE_BACKTRACE_DATA false
#define ENABLE_CHECK_MESSAGE_OVERFLOW false

#if USE_CRITICAL_CHECK == true
#define CRITICAL_CHECK(assertion) matrix::_hook_check(assertion)
#else
#define CRITICAL_CHECK(assertion)
#endif

#define SIZE_AUGMENT 192

#define MEMORY_OVER_LIMIT (1024 * 1024 * 150L)    // 150M

#define PROCESS_BUSY_INTERVAL 40 * 1000L
#define PROCESS_NORMAL_INTERVAL 150 * 1000L
#define PROCESS_LESS_NORMAL_INTERVAL 300 * 1000L
#define PROCESS_IDLE_INTERVAL 800 * 1000L

#define PTR_SPLAY_MAP_CAPACITY 10240
#define STACK_SPLAY_MAP_CAPACITY 1024

#define MEMHOOK_BACKTRACE_MAX_FRAMES MAX_FRAME_SHORT

#define POINTER_MASK 48

#ifndef LIKELY
#define LIKELY(cond) (__builtin_expect(!!(cond), 1))
#endif

#ifndef UNLIKELY
#define UNLIKELY(cond) (__builtin_expect(!!(cond), 0))
#endif

#define EXPORT extern __attribute__ ((visibility ("default")))

#define EXPORT_C extern "C" __attribute__ ((visibility ("default")))

#define NELEM(...) ((sizeof(__VA_ARGS__)) / (sizeof(__VA_ARGS__[0])))

#define HOOK_CHECK(assertion)                              \
  if (__builtin_expect(!(assertion), false)) {             \
    abort();                                               \
  }

#if ENABLE_CHECK_MESSAGE_OVERFLOW == true
#define CHECK_MESSAGE_OVERFLOW(assertion) HOOK_CHECK(assertion)
#else
#define CHECK_MESSAGE_OVERFLOW(assertion)
#endif

#define ReservedSize(AugmentExp) ((1 << AugmentExp))

#endif //LIBMATRIX_JNI_MACROS_H
