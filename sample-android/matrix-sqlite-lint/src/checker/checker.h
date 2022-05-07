

//
 on 2017/6/22
//

#ifndef SQLITE_LINT_CHECKER_CHECKER_H
#define SQLITE_LINT_CHECKER_CHECKER_H

#include <vector>
#include "core/lint_env.h"
#include "sqlite_lint.h"

namespace sqlitelint {

    typedef enum {
        kAfterInit,
        kSample,
        kUncheckedSql,
        kEverySql,
    } CheckScene;

    class Checker {
    public:
        virtual ~Checker();

        // TODO const SQLiteLintEnv& env
        // Check will be called according to the CheckScene of this checker
        // parameter env and sql_info will offers much info to the check algorithm
        // Output issues when check to find some places against the best practice of SQLite3 which this checker proposals
        virtual void Check(LintEnv& env, const SqlInfo& sql_info, std::vector<Issue>* issues) = 0;

        // see enum CheckScene
        virtual CheckScene GetCheckScene() = 0;

        // Only the checker with CheckScene kSample, need override this method
        // And the return value will determine check rate
        // For example , if return 100, the checker will be scheduled to check every 100 sqls executed
        virtual int GetSqlCntToSample();
    };
}

#endif //SQLITE_LINT_CHECKER_CHECKER_H
