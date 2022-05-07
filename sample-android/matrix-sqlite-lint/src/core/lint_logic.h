#ifndef SQLITE_LINT_LINT_LOGIC_H
#define SQLITE_LINT_LINT_LOGIC_H

#include <string>
#include <vector>
#include "lint_info.h"

namespace sqlitelint {
    // Gen id to indentify a issue
    // identity_info can differ this issue from others in a certain checker(check_name)
    std::string GenIssueId(const std::string& db_file_name, const std::string& checker_name, const std::string& identity_info);

    // Now only "select", "insert", "update", "delete", "create" supported
    bool IsSqlSupportCheck(const std::string& sql);

    constexpr static const char* const  kAutoIndexPrefix = "sqlite_autoindex_";
    static const int kAutoIndexPrefixLen = strlen(kAutoIndexPrefix);
    bool IsSQLite3AutoIndex(const std::string& index);

    void DumpQueryPlans(const std::vector<Record> & plans);
}

#endif //SQLITE_LINT_LINT_LOGIC_H
