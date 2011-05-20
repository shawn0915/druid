package com.alibaba.druid.sql.dialect.mysql.visitor;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLLiteralExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;

public class MySqlParameterizedOutputVisitor extends MySqlOutputVisitor {

    public MySqlParameterizedOutputVisitor(Appendable appender){
        super(appender);
    }
    
    public void println() {
        print(' ');
    }

    public boolean visit(SQLInListExpr x) {
        x.getExpr().accept(this);

        if (x.isNot()) {
            print(" NOT IN (##)");
        } else {
            print(" IN (##)");
        }
        return false;
    }
    
    public boolean visit(SQLBinaryOpExpr x) {
        if (x.getLeft() instanceof SQLLiteralExpr && x.getRight() instanceof SQLLiteralExpr) {
            print(x.getLeft().toString());
            print(' ');
            print(x.getOperator().name);
            print(' ');
            print(x.getRight().toString());
            return false;
        }
        
        return super.visit(x);
    }
    
    public boolean visit(SQLIntegerExpr x) {
        print('?');
        return false;
    }

    @Override
    public boolean visit(MySqlInsertStatement x) {
        print("INSERT ");

        if (x.isLowPriority()) {
            print("LOW_PRIORITY ");
        }

        if (x.isDelayed()) {
            print("DELAYED ");
        }

        if (x.isHighPriority()) {
            print("HIGH_PRIORITY ");
        }

        if (x.isIgnore()) {
            print("IGNORE ");
        }

        print("INTO ");

        x.getTableName().accept(this);

        if (x.getColumns().size() > 0) {
            print(" (");
            for (int i = 0, size = x.getColumns().size(); i < size; ++i) {
                if (i != 0) {
                    print(", ");
                }
                x.getColumns().get(i).accept(this);
            }
            print(")");
        }

        if (x.getValuesList().size() != 0) {
            print(" VALUES ");
            int size = x.getValuesList().size();
            if (size == 0) {
                print("()");
            } else {
                for (int i = 0; i < 1; ++i) {
                    if (i != 0) {
                        print(", ");
                    }
                    x.getValuesList().get(i).accept(this);
                }
            }
        }
        if (x.getQuery() != null) {
            print(" ");
            x.getQuery().accept(this);
        }

        if (x.getDuplicateKeyUpdate().size() != 0) {
            print(" ON DUPLICATE KEY UPDATE ");
            printAndAccept(x.getDuplicateKeyUpdate(), ", ");
        }

        return false;
    }
}
