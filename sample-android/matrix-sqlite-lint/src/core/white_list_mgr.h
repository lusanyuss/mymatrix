

//
// if the concerned target is in white list, checker will not publish issues
// one db one lint one white list manager
//
// Author: liyongjie

//

#ifndef SQLITE_LINT_CORE_LINT_WHITE_LIST_H
#define SQLITE_LINT_CORE_LINT_WHITE_LIST_H

#include <string>
#include <set>
#include <map>

namespace sqlitelint {

    class WhiteListMgr {
    public:
        bool IsInWhiteList(const std::string& checker_name, const std::string& target) const;
        void SetWhiteList(const std::map<std::string, std::set<std::string>>& white_list);
    private:
        std::map<std::string, std::set<std::string>> white_list_;
    };
}

#endif //SQLITE_LINT_CORE_LINT_WHITE_LIST_H
