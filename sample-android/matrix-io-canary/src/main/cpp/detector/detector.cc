

//
//

#include "detector.h"
#include "comm/io_canary_utils.h"

namespace iocanary {

    Issue::Issue(IssueType type, IOInfo file_io_info) : type_(type), key_(GenKey(file_io_info)), file_io_info_(file_io_info) {
        repeat_read_cnt_ = 0;
        stack = file_io_info.java_context_.stack_;
    }

    std::string Issue::GenKey(const IOInfo &file_io_info) {
        return MD5(file_io_info.path_ + ":" + GetLatestStack(file_io_info.java_context_.stack_, 4));
    }

    void FileIODetector::PublishIssue(const Issue &target, std::vector<Issue>& issues) {
        if (IsIssuePublished(target.key_)) {
            return;
        }

        issues.push_back(target);

        MarkIssuePublished(target.key_);
    }

    void FileIODetector::MarkIssuePublished(const std::string &key) {
        published_issue_set_.insert(key);
    }

    bool FileIODetector::IsIssuePublished(const std::string &key) {
        return published_issue_set_.find(key) != published_issue_set_.end();
    }

    FileIODetector::~FileIODetector() {}
}
