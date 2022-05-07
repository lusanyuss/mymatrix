

//
// Prepared statement is common and efficient measure to improve performance
// It may avoid repeat and redundant jobs of sqlite3_prepare(), and may give a significant performance improvement
// This checker is to remind you to make good use of prepared statement when a exact same sql statement was executed
// continuously
// This checker will be scheduled every 30[?] sqls, a sample scene, check algorithm propably as following:
// 1. look backward to include a little more sqls in order to handle the boundary, see HandleTheBoundary
// 2. grouped by the sql statement, see MakeGroup
// 3. To a certain sql statement group, find the section , in which :
//    1) all the internals of the adjacent executions is shorter than kContinuityInternalThreshold
//    2) if and the count of sqls is >= kContinuousCountThreshold, then sugguest to use prepared statement to enhance
//    see ScanCheck
//
// Author: liyongjie

//

#ifndef SQLITE_LINT_CHECKER_COMPILE_STATEMENT_MISS_CHECKER_H
#define SQLITE_LINT_CHECKER_COMPILE_STATEMENT_MISS_CHECKER_H

#include <vector>
#include "checker.h"

namespace sqlitelint {

    class PreparedStatementBetterChecker : public Checker {
    public:
        virtual void Check(LintEnv& env, const SqlInfo& sql_info, std::vector<Issue>* issues) override;
        virtual CheckScene GetCheckScene() override;

        // 30 is a rough decision
        virtual int GetSqlCntToSample() override;

    private:
        constexpr static const char* const  kCheckerName = CheckerName::kPreparedStatementBetterCheckerName;

        // if the internal of the adjacent executions is longer than this threhold
        // Then take it as discontinous
        constexpr static const int kContinuityInternalThreshold = 100;

        // if a same sql statement continuously executes over kContinuousCountThreshold
        // the checker will publish a issue
        constexpr static const int kContinuousCountThreshold = 3;

        void HandleTheBoundary(LintEnv& env, std::vector<SqlInfo>* to_check_sqls);

        void MakeGroup(const std::vector<SqlInfo>& to_check_sqls, std::map<std::string, std::vector<SqlInfo*>>* buckets);

        // Scan the grouped_sqls, find a section meet 3.1 and 3.2 conditions described in the header of this file
        // If found one, then break the scan. because it's a same sql statement in this group
        void ScanCheck(const std::vector<SqlInfo*>& grouped_sqls, std::vector<SqlInfo*>* section);

        void PublishIssue(const LintEnv& env, const std::vector<SqlInfo*>& section, std::vector<Issue>* issues);

        std::string recently_reported_id;
    };
}

#endif //SQLITE_LINT_CHECKER_COMPILE_STATEMENT_MISS_CHECKER_H
