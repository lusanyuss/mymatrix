

//
//

#ifndef SQLITE_LINT_CHECKER_EQP_WHERE_CLAUSE_TABLE_COLUMN_FINDER_H
#define SQLITE_LINT_CHECKER_EQP_WHERE_CLAUSE_TABLE_COLUMN_FINDER_H

#include <string>
#include <set>
#include "core/lint_info.h"

namespace sqlitelint {
    class WhereClauseTableColumnFinder {
    public:
        WhereClauseTableColumnFinder(const Expr *target_where, const std::string &target_table, const std::string &target_table_alias
				, const std::vector<ColumnInfo>& target_table_columns);
        ~WhereClauseTableColumnFinder();

        bool HasTargetTableColumnInWhereClause() const;
        bool IsTargetTableHasConstantExpression() const;
        const std::vector<std::string>& GetTargetTableColumnsInWhereClause() const;

	private:
		void VisitExpr(const Expr *p);
		void VisitExprList(const ExprList *p);
		void VisitSrcList(const SrcList *p);
		void VisitToken(const Token& p, std::string* r);
		void VisitSelect(const Select *p);
		void Process(const std::string &table, const std::string &column);
        void ProcessConstantExpr(const Expr *p);

		std::string target_table_;
		std::string target_table_alias_;

		//指定表的列是否在where中出现
		bool has_target_table_column_in_where_clause_ = false;
		//指定表是否有常量表达式
		bool is_target_table_has_constant_expression_ = false;
		//指定表在where中出现的列
		std::vector<std::string> target_table_columns_in_where_clause_;
		//指定表的列，env中传入
		std::set<std::string> target_table_columns_;
    };
}

#endif //SQLITE_LINT_CHECKER_EQP_WHERE_CLAUSE_TABLE_COLUMN_FINDER_H
