

//
//

#ifndef SQLITE_LINT_CHECKER_EQP_SELECT_TREE_HELPER_H
#define SQLITE_LINT_CHECKER_EQP_SELECT_TREE_HELPER_H

#include <string>
#include <vector>
#include <map>
#include "core/lint_info.h"

namespace sqlitelint {
    class SelectTreeHelper {
    public:
        SelectTreeHelper(Select *select);
        ~SelectTreeHelper();
        void Process();
        Select* GetSelect(const std::string& table);
        bool HasUsingOrOn();
        bool HasFuzzyMatching();
        bool HasBitOperation();
        bool HasOr();
        bool HasIn();

    private:
        void ProcessExprList(const ExprList *p);
        void ProcessExpr(const Expr *p);
        void ProcessSrcList(const SrcList *p, Select *s);
        void ProcessSelect(Select *p);
		void AddSelectTree(const char *table, Select *p);
        void ProcessToken(const Token &p, int op);

        std::map<std::string, std::vector<Select*>> select_tree_map_;
        Select *select_;
        bool has_using_or_on;
        bool has_fuzzy_matching;
        bool has_bit_operation;
        bool has_or;
        bool has_in;

    };
}

#endif //SQLITE_LINT_CHECKER_EQP_SELECT_TREE_HELPER_H
