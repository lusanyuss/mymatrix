

//
// Author: liyongjie
//

#ifndef MATRIX_IO_CANARY_IO_CANARY_UTILS_H
#define MATRIX_IO_CANARY_IO_CANARY_UTILS_H

#include <stdlib.h>

#include <string>
#include <vector>

namespace iocanary {
    int64_t GetSysTimeMicros();
    int64_t GetSysTimeMilliSecond();

    int64_t GetTickCountMicros();
    int64_t GetTickCount();

    intmax_t GetMainThreadId();
    intmax_t GetCurrentThreadId();
    bool IsMainThread();

    std::string GetLatestStack(const std::string& stack, int count);
    void Split(const std::string &src_str, std::vector<std::string> &sv, const char delim, int cnt = 0);
    int GetFileSize(const char* file_path);
    std::string MD5(std::string);
}


#endif //MATRIX_IO_CANARY_IO_CANARY_UTILS_H
