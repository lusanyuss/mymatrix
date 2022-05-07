

#ifndef LIBMATRIX_JNI_UTILS_H
#define LIBMATRIX_JNI_UTILS_H

#include <cstdint>
#include <unwindstack/Unwinder.h>
#include "Backtrace.h"

#define likely(x)   __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)

uint64_t hash_uint64(uint64_t *p_pc_stacks, size_t stack_size);
// enhance: hash to 64 bits
uint64_t hash_backtrace_frames(wechat_backtrace::Backtrace *stack_frames);
uint64_t hash_str(const char * str);
uint64_t hash_combine(uint64_t l, uint64_t r);

/*
 * Estimated collision rate: 0.3%
 * Total 50173 hash found 74 hash collision involving 148 different backtrace.
 */
inline uint64_t hash_frames(wechat_backtrace::Frame *frame, size_t size) {
    if (unlikely(frame == nullptr || size == 0)) {
        return 1;
    }
    size_t max = std::min((size_t) 16, size);
    uint64_t sum = size;
    for (size_t i = 0; i < max; i++) {
        sum += frame[i].pc << (i << 1);
    }
    return (uint64_t) sum;
}


#endif //LIBMATRIX_JNI_UTILS_H
