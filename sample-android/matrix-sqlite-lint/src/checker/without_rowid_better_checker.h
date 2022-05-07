

//
// Using "Without Rowid" feature may sometimes bring space and performance advantages
// When the table meets the following condition, we advice you use "Without RowId" with the table:
// 1. has non-integer or composite (multi-column) PRIMARY KEYs
// 2. individual rows are not too large. The strict judgement algorithm now is that there's no text or blob type non-primary-key columns
// This checker works on advising the developer to use "Without Rowid" at a proper time.
// And publishes Tip-Level issues but it's recommended that you run tests to see if the "Without Rowid" helps.
// eg.
// CREATE TABLE IF NOT EXISTS wordcount(
//        word TEXT PRIMARY KEY,
//        cnt INTEGER
// ) WITHOUT ROWID;
//
// Author: liyongjie

//

#ifndef SQLITE_LINT_CHECKER_WITHOUT_ROWID_BETTER_CHECKER_H
#define SQLITE_LINT_CHECKER_WITHOUT_ROWID_BETTER_CHECKER_H

#include <vector>
#include "checker.h"

namespace sqlitelint {

    class WithoutRowIdBetterChecker : public Checker {
    public:
        virtual void Check(LintEnv& env, const SqlInfo& sql_info, std::vector<Issue>* issues) override;
        virtual CheckScene GetCheckScene() override;

    private:

        constexpr static const char* const  kCheckerName = CheckerName::kWithoutRowIdBetterCheckerName;
        constexpr static const char* const  kWithoutRowIdKeyWord = "without rowid";

        void PublishIssue(const LintEnv& env, const std::string& table_name, std::vector<Issue>* issues);
        bool IsWithoutRowIdBetter(const TableInfo& table_info);
    };

}

#endif //SQLITE_LINT_CHECKER_WITHOUT_ROWID_BETTER_CHECKER_H
