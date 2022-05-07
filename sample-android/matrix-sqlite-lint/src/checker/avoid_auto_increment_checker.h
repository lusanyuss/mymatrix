

//
// "The AUTOINCREMENT keyword imposes extra CPU, memory, disk space, and disk I/O overhead
// and should be avoided if not strictly needed. It is usually not needed"
// This checker is to find this issue
//
// Author : liyongjie

//

#ifndef SQLITE_LINT_CHECKER_AVOID_AUTO_INCREMENT_CHECKER_H
#define SQLITE_LINT_CHECKER_AVOID_AUTO_INCREMENT_CHECKER_H

#include <vector>
#include "checker/checker.h"

namespace sqlitelint {

    class AvoidAutoIncrementChecker : public Checker {
    public:
        virtual void Check(LintEnv& env, const SqlInfo& sql_info, std::vector<Issue>* issues) override;
        virtual CheckScene GetCheckScene() override;
    private:
        constexpr static const char* const  kCheckerName = CheckerName::kAvoidAutoIncrementCheckerName;
        constexpr static const char* const  kAutoIncrementKeyWord = "autoincrement";

        void PublishIssue(const LintEnv& env, const std::string& table_name, std::vector<Issue>* issues);
    };
}

#endif //SQLITE_LINT_CHECKER_AVOID_AUTO_INCREMENT_CHECKER_H
