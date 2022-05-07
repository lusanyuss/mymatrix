

//
// If the buffer of write/read is small, it may not make good use of the capacity of read/write.
// This detector is to find such a issue.
//
//

#ifndef MATRIX_IO_CANARY_SMALL_BUFFER_DETECTOR_H
#define MATRIX_IO_CANARY_SMALL_BUFFER_DETECTOR_H

#include "detector.h"

namespace iocanary {

    class FileIOSmallBufferDetector : public FileIODetector {
    public:
        virtual void Detect(const IOCanaryEnv& env, const IOInfo& file_io_info, std::vector<Issue>& issues) override ;

        constexpr static const IssueType kType = IssueType::kIssueSmallBuffer;
    };
}
#endif //MATRIX_IO_CANARY_SMALL_BUFFER_DETECTOR_H
