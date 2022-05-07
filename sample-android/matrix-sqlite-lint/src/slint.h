

//
// The data structs exposed to SDK-Level; eg.android, iOS, windows
//
//

#ifndef SQLITE_LINT_SLINT_H
#define SQLITE_LINT_SLINT_H

#include <string>

namespace sqlitelint {

    // CheckerName can be used to configured the white list
    class CheckerName {
    public:
        static constexpr const char* const kExplainQueryPlanCheckerName = "ExplainQueryPlanChecker";
        static constexpr const char* const kAvoidAutoIncrementCheckerName = "AvoidAutoIncrementChecker";
        static constexpr const char* const kAvoidSelectAllCheckerName = "AvoidSelectAllChecker";
        static constexpr const char* const kWithoutRowIdBetterCheckerName = "WithoutRowIdBetterChecker";
        static constexpr const char* const kPreparedStatementBetterCheckerName = "PreparedStatementBetterChecker";
        static constexpr const char* const kRedundantIndexCheckerName = "RedundantIndexChecker";
    };

    // IssueLevel represents the scale of the issue
    // When the issue is over kSuggestion level, it's worth paying attention to
    typedef enum {
        kPass = 0,
        kTips,
        kSuggestion,
        kWarning,
        kError,
    } IssueLevel;

    typedef enum {
        kExplainQueryScanTable = 1,
        kExplainQueryUseTempTree,
        kExplainQueryTipsForLargerIndex,
        kAvoidAutoIncrement,
        kAvoidSelectAllChecker,
        kWithoutRowIdBetter,
        kPreparedStatementBetter,
        kRedundantIndex,
    } IssueType;

    // The information of the problem the checkers found
    // Issue will be published to the SDK level
    class Issue {
    public:
        std::string id;
        std::string db_path;
        IssueType type;
        IssueLevel level;
        std::string sql;
        std::string table;
        int64_t create_time;
        std::string desc;
        std::string detail;
        std::string advice;
        std::string ext_info;
        long sql_time_cost = 0;
        bool is_in_main_thread = false;
    };
}
#endif //SQLITE_LINT_SLINT_H
