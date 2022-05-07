#include <comm/log/logger.h>
#include "comm/lint_util.h"
#include "lint_logic.h"

namespace sqlitelint {

    std::string GenIssueId(const std::string& db_file_name, const std::string& checker_name, const std::string& identity_info) {
        return MD5(db_file_name + "_" + checker_name + "_" + identity_info);
    }

    bool IsSqlSupportCheck(const std::string& sql) {
        if (sql.length() < 6) {
            return false;
        }
        std::string keyword = sql.substr(0, 6);
        if (keyword == "select" || keyword == "insert" || keyword == "update"
            || keyword == "delete" || keyword == "replac") {
            return true;
        }
        return false;
    }

    bool IsSQLite3AutoIndex(const std::string& index) {
        return strncmp(index.c_str(), kAutoIndexPrefix, kAutoIndexPrefixLen) == 0;
    }

    void DumpQueryPlans(const std::vector<Record> & plans) {
        std::string print;
        for (auto & record : plans) {
            print.append(to_string(record.select_id_));
            print.append(to_string(record.order_));
            print.append(to_string(record.from_ ));
            print.append(record.detail_).append("\n");
        }
        sDebug("DumpQueryPlans :\n %s", print.c_str());
    }
}
