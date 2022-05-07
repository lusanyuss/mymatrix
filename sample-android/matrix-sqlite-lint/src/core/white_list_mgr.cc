

//
// Author: liyongjie

//

#include "white_list_mgr.h"
#include "comm/lint_util.h"
#include "comm/log/logger.h"

namespace sqlitelint {

    bool WhiteListMgr::IsInWhiteList(const std::string &checker_name, const std::string &target) const {
        const auto& it = white_list_.find(checker_name);
        if (it == white_list_.end()) {
            return false;
        }
        std::string low_case = target;
        ToLowerCase(low_case);
        if (it->second.find(low_case) == it->second.end()) {
            return false;
        }

        return true;
    }

    void WhiteListMgr::SetWhiteList(
            const std::map<std::string, std::set<std::string>> &white_list) {
        white_list_.clear();
        for (const auto& it: white_list) {
            white_list_[it.first] = std::set<std::string>();
            for (const auto& element : it.second) {
                std::string low_case_element = element;
                ToLowerCase(low_case_element);
                white_list_[it.first].insert(low_case_element);
            }
        }
    }
}
