

//
// Author: liyongjie

//

#include "without_rowid_better_checker.h"
#include "comm/lint_util.h"
#include "core/lint_logic.h"
#include <comm/log/logger.h>

namespace sqlitelint {

    void WithoutRowIdBetterChecker::Check(LintEnv &env, const SqlInfo &sql_info,
                                          std::vector<Issue> *issues) {
        std::vector<TableInfo> tables = env.GetTablesInfo();

        sVerbose("WithoutRowIdBetterChecker::Check tables count: %zu", tables.size());

        std::string create_sql;
        for (TableInfo table_info : tables) {
            if (env.IsInWhiteList(kCheckerName, table_info.table_name_)) {
                sVerbose("WithoutRowIdBetterChecker::Check in white list:%s ", table_info.table_name_.c_str());
                continue;
            }

            create_sql = table_info.create_sql_;
            ToLowerCase(create_sql);
            // already use Without RowId
            if ( create_sql.find(kWithoutRowIdKeyWord) != std::string::npos ) {
                continue;
            }

            if ( IsWithoutRowIdBetter(table_info) ) {
                PublishIssue(env, table_info.table_name_, issues);
            }
        }
    }

    CheckScene WithoutRowIdBetterChecker::GetCheckScene() {
        return CheckScene::kAfterInit;
    }

    void WithoutRowIdBetterChecker::PublishIssue(const LintEnv& env, const std::string &table_name,
                                                 std::vector<Issue> *issues) {
        Issue issue;
        issue.id = GenIssueId(env.GetDbFileName(), kCheckerName, table_name);
        issue.db_path = env.GetDbPath();
        issue.create_time = GetSysTimeMillisecond();
        issue.level = IssueLevel::kTips;
        issue.type = IssueType::kWithoutRowIdBetter;
        issue.table = table_name;
        issue.desc = "Table(" + table_name + ") can use \"Without Rowid\" feature to optimize.";
        issue.advice = "It is recommend to use \"Without Rowid\" feature in this table."
                        "But also you can run tests to see if the \"Without Rowid\" helps";

        issues->push_back(issue);
    }

    bool WithoutRowIdBetterChecker::IsWithoutRowIdBetter(const TableInfo &table_info) {
        int primary_key_column_cnt = 0;
        bool has_integer_primary_key = false;
        bool has_large_columns = false;

        for(ColumnInfo column_info : table_info.columns_) {
            if (column_info.is_primary_key_) {
                primary_key_column_cnt ++;
                //TODO too ugly
                if (CompareIgnoreCase(column_info.type_, "integer") == 0) {
                    has_integer_primary_key = true;
                }
            } else {
                //non-primary-key, further judge if it's a so-called large column
                //Here strictly take text and blob type as large
                if (!has_large_columns) {
                    if (CompareIgnoreCase(column_info.type_, "text") == 0
                        || CompareIgnoreCase(column_info.type_, "blob") == 0) {
                        has_large_columns = true;

                        // no meet condition 2
                        // the sine qua non
                        break;
                    }
                }
            }
        }

        sDebug("WithoutRowIdBetterChecker::IsWithoutRowIdBetter table:%s primary_key_column_cnt:%d has_integer_primary_key:%d has_large_columns:%d",
                table_info.table_name_.c_str(), primary_key_column_cnt, has_integer_primary_key, has_large_columns);


        // condition 2: individual rows are not too large
        if (!has_large_columns
                // with condition 1: has non-integer or composite (multi-column) PRIMARY KEYs
                && ((primary_key_column_cnt == 1 && !has_integer_primary_key) || primary_key_column_cnt > 1)) {
            return true;
        }

        return false;
    }
}
