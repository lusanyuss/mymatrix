

//
//  receive and manage the executed sqls -> pre-process sql info
//  -> schedule the checkers -> output issues to the SDK level
//
//  checkers are scheduled to check in a single thread
//
//  author: liyongjie
//

#ifndef SQLITE_LINT_CORE_LINT_H
#define SQLITE_LINT_CORE_LINT_H

#include <condition_variable>
#include <deque>
#include <mutex>
#include <map>
#include <vector>
#include <thread>
#include "checker/checker.h"
#include "comm/lru_cache.h"
#include "core/lint_env.h"
#include "sqlite_lint.h"

namespace sqlitelint {

    class Lint {
    public:
        Lint(const char* db_path, OnPublishIssueCallback issued_callback);
        ~Lint();
        Lint(const Lint&) = delete;

        // collects executed or to checked sqls
        void NotifySqlExecution(const char *sql, const long time_cost, const char* ext_info);

        // the white list to avoid the checkers's mistake as much as possible
        void SetWhiteList(const std::map<std::string, std::set<std::string>>& white_list);

        void RegisterChecker(const std::string& checker_name);

        // enable checkers
		void RegisterChecker(Checker* checker);
    private:
        bool exit_;
        std::thread* check_thread_;
        std::thread* init_check_thread_;

        // A function pointer to callback to the SDK level
        const OnPublishIssueCallback issued_callback_;
        LintEnv env_;
        // The available(enabled) checkers
        std::map<CheckScene, std::vector<Checker*>> checkers_;
        // Manage the collected sqls in a queue; FIFO
        std::deque<std::unique_ptr<SqlInfo>> queue_;
        std::mutex queue_mutex_;
        std::condition_variable queue_cv_;
        // Use this to implement the unchecked logic
        LRUCache<std::string, bool> checked_sql_cache_;

        // Main schedule and check logic
        void Check();

        void InitCheck();

        // take to sql info to check
        // block if empty
        int TakeSqlInfo(std::unique_ptr<SqlInfo> &sql_info);

        // Pre process and get infos like sql-tree and so on
        // Checkers will need this pre-process info
        bool PreProcessSqlInfo(SqlInfo* sql_info);

        void PreProcessSqlString(std::string &sql);

        // Schedule the checkers of check_scene to process their check algorithm
        void ScheduleCheckers(const CheckScene check_scene, const SqlInfo& sql_info, std::vector<Issue>* published_issues);
    };
}
#endif /* SQLITE_LINT_CORE_LINT_H */
