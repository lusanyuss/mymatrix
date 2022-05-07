

//
// A tree data struct to construct the explain query plan result list,
// which can help analyze the plans.
//
// Author: liyongjie

//

#ifndef SQLITE_LINT_CHECKER_EQP_EXPLAIN_QUERY_PLAN_TREE_H
#define SQLITE_LINT_CHECKER_EQP_EXPLAIN_QUERY_PLAN_TREE_H

#include <core/lint_info.h>
#include <regex>

namespace sqlitelint {

    class EQPTreeNode {
    public:
        explicit EQPTreeNode(const Record& main_record);
        void AddChild(EQPTreeNode* child);
        const std::vector<EQPTreeNode*>& GetChilds() const ;

        const Record& GetMainRecord() const ;

        void AddRecordToGroup(const Record& record);
        const std::vector<Record>& GetGroupRecords() const;

    private:
        const Record& main_record_;
        std::vector<EQPTreeNode*> childs_;
        std::vector<Record> group_records_;
    };

    class ExplainQueryPlanTree {
    public:
        explicit ExplainQueryPlanTree(const QueryPlan& query_plan);
        ~ExplainQueryPlanTree();
        EQPTreeNode* GetRootNode();
        void DumpTree();
    private:
        static const std::regex kExtractSelectIdRgx;
        EQPTreeNode* root_node_;

        EQPTreeNode* BuildFantasyEQPTree(const std::vector<Record>& plans, int* start_index);
        void ParseCompoundRecord(const Record& record, std::vector<int>* sub_query_select_ids);
        void ReleaseTree(EQPTreeNode* node);
        void DoDumpTree(const EQPTreeNode *node, const int level, std::string* print);
    };
}

#endif //SQLITE_LINT_CHECKER_EQP_EXPLAIN_QUERY_PLAN_TREE_H
