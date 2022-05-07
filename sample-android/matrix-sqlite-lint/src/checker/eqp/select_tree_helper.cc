

//
//

#include "select_tree_helper.h"
#include "comm/log/logger.h"

namespace sqlitelint {

	SelectTreeHelper::SelectTreeHelper(Select *select) : has_using_or_on(false)
            ,has_bit_operation(false),has_fuzzy_matching(false),has_or(false),has_in(false){
		select_ = select;
	}

	void SelectTreeHelper::Process() {
		ProcessSelect(select_);
		sVerbose("SelectTreeHelper::init done");
	}

    Select* SelectTreeHelper::GetSelect(const std::string& table) {
		std::vector<Select*> selects = select_tree_map_[table];
		if (selects.size() > 0) {
			sVerbose("getSelect: table=%s", table.c_str());
			Select* select = selects[0];
			selects.erase(selects.begin());
			return select;
		}

		sWarn("SelectTreeHelper: not find select tree, table=%s", table.c_str());
		return nullptr;
    }

	void SelectTreeHelper::ProcessSelect(Select *p) {
		if (!p)return;
		if (p->pPrior) {
			ProcessSelect(p->pPrior);
		}
		ProcessExprList(p->pEList);
		if (p->pSrc&&p->pSrc->nSrc) {
			ProcessSrcList(p->pSrc, p);
		}
		if (p->pWhere) {
			ProcessExpr(p->pWhere);
		}
		if (p->pGroupBy) {
			ProcessExprList(p->pGroupBy);
		}
		if (p->pOrderBy) {
			ProcessExprList(p->pOrderBy);
		}
		if (p->pHaving) {
			ProcessExpr(p->pHaving);
		}
		if (p->pLimit) {
			ProcessExpr(p->pLimit);
		}
		if (p->pOffset) {
			ProcessExpr(p->pOffset);
		}
	}

	void SelectTreeHelper::ProcessExprList(const ExprList *p) {
		if (!p)return;
		for (int i = 0; i < p->nExpr; i++) {
			ProcessExpr(p->a[i].pExpr);
		}
	}

	void SelectTreeHelper::ProcessExpr(const Expr *p) {
		if (!p)return;

		ProcessExpr(p->pLeft);
        ProcessToken(p->token,p->op);
		ProcessExpr(p->pRight);

		if (p->pSelect) {
			ProcessSelect(p->pSelect);
		}
		ProcessExprList(p->pList);
	}

	//from
	void SelectTreeHelper::ProcessSrcList(const SrcList *p, Select *s) {
		if (!p)return;
		for (int i = 0; i < p->nSrc; i++) {
			AddSelectTree(p->a[i].zName, s);

			ProcessSelect(p->a[i].pSelect);

            if (p->a[i].pUsing) {
                has_using_or_on = true;
            }

			if (p->a[i].pOn) {
                has_using_or_on = true;
				ProcessExpr(p->a[i].pOn);
			}
		}
	}

    void SelectTreeHelper::ProcessToken(const Token& p, int op) {
        switch (op) {
            case TK_BITAND:
            case TK_BITOR:
            case TK_BITNOT:
            case TK_LSHIFT:
            case TK_RSHIFT:
                has_bit_operation = true;
                break;
            case TK_LIKE_KW:
                has_fuzzy_matching = true;
                break;
            case TK_OR:
                has_or = true;
                break;
            case TK_IN:
                has_in = true;
                break;
        }
    }

	void SelectTreeHelper::AddSelectTree(const char *table, Select *p) {
        if(!table){
            return;
        }
		if (select_tree_map_.find(table) == select_tree_map_.end()) {
			std::vector<Select*> selects;
			select_tree_map_[table] =  selects;
		}
		select_tree_map_[table].push_back(p);
	}

	SelectTreeHelper::~SelectTreeHelper() {
        for(auto it = select_tree_map_.begin();it != select_tree_map_.end();it++) {
            it->second.clear();
        }
        select_tree_map_.clear();
	}

    bool SelectTreeHelper::HasUsingOrOn() {
        return has_using_or_on;
    }

    bool SelectTreeHelper::HasFuzzyMatching() {
        return has_fuzzy_matching;
    }


    bool SelectTreeHelper::HasBitOperation() {
        return has_bit_operation;
    }

    bool SelectTreeHelper::HasOr() {
        return has_or;
    }

    bool SelectTreeHelper::HasIn() {
        return has_in;
    }
}
