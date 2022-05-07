

//

//

#include "avoid_auto_increment_checker.h"
#include "core/lint_logic.h"
#include "comm/lint_util.h"
#include <comm/log/logger.h>

namespace sqlitelint {

    void AvoidAutoIncrementChecker::Check(LintEnv &env, const SqlInfo &sql_info,
                                          std::vector<Issue> *issues) {
        std::vector<TableInfo> tables = env.GetTablesInfo();

        sVerbose("AvoidAutoIncrementChecker::Check tables count: %zu", tables.size());

        std::string create_sql;

        for (const TableInfo& table_info : tables) {
            if (env.IsInWhiteList(kCheckerName, table_info.table_name_)) {
                sVerbose("AvoidAutoIncrementChecker::Check in white list: %s", table_info.table_name_.c_str());
                continue;
            }

            create_sql = table_info.create_sql_;
            ToLowerCase(create_sql);
            if (create_sql.find(kAutoIncrementKeyWord) != std::string::npos) {
                PublishIssue(env, table_info.table_name_, issues);
            }
        }
    }

    CheckScene AvoidAutoIncrementChecker::GetCheckScene() {
        return CheckScene::kAfterInit;
    }

    void AvoidAutoIncrementChecker::PublishIssue(const LintEnv& env, const std::string &table_name,
                                                 std::vector<Issue> *issues) {
        sVerbose("AvoidAutoIncrementChecker::PublishIssue table: %s", table_name.c_str());

        std::string desc = "Table(" + table_name + ") has a column which is AutoIncrement."
                            + "It's not really recommended.";

        Issue issue;
        issue.id = GenIssueId(env.GetDbFileName(), kCheckerName, table_name);
        issue.db_path = env.GetDbPath();
        issue.create_time = GetSysTimeMillisecond();
        issue.level = IssueLevel::kTips;
        issue.type = IssueType::kAvoidAutoIncrement;
        issue.table = table_name;
        issue.desc = desc;

        issues->push_back(issue);
    }
}
