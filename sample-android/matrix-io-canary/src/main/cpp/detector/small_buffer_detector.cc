

//
//

#include "small_buffer_detector.h"

namespace iocanary {

    void FileIOSmallBufferDetector::Detect(const IOCanaryEnv &env, const IOInfo &file_io_info,
                                           std::vector<Issue>& issues) {
        //__android_log_print(ANDROID_LOG_ERROR, "FileIOSmallBufferDetector", "Detect buffer_size:%d threshold:%d op_cnt:%d rw_cost:%d",
          //                  file_io_info.buffer_size_, env.GetSmallBufferThreshold(), file_io_info.op_cnt_, file_io_info.max_continual_rw_cost_time_μs_);

        if (file_io_info.op_cnt_ > env.kSmallBufferOpTimesThreshold && (file_io_info.op_size_ / file_io_info.op_cnt_) < env.GetSmallBufferThreshold()
                && file_io_info.max_continual_rw_cost_time_μs_ >= env.kPossibleNegativeThreshold) {

            PublishIssue(Issue(kType, file_io_info), issues);
        }
    }
}
