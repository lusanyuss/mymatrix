

//

//

#ifndef SQLITE_LINT_LINT_MANAGER_H
#define SQLITE_LINT_LINT_MANAGER_H

#include <map>
#include <mutex>

#include "sqlite_lint.h"

namespace sqlitelint {

    class Lint;

    // A singleton and it manage the lint
    class LintManager {
    public :
        static LintManager* Get();
        static void Release();
        void Install(const char* db_path, OnPublishIssueCallback issued_callback);
        void Uninstall(const std::string& db_path);
        void UninstallAll();
        void NotifySqlExecution(const char* db_path, const char* sql, long time_cost, const char* ext_info);
        void SetWhiteList(const char* db_path, const std::map<std::string, std::set<std::string>>& white_list);
        void EnableChecker(const char* db_path, const std::string& checker_name);

    private:
        LintManager(){};
        ~LintManager(){};
    private:
        std::map<const std::string, Lint*> lints_;
        static std::mutex lints_mutex_;
        static LintManager *instance_;
    };
}
#endif //SQLITE_LINT_LINT_MANAGER_H
