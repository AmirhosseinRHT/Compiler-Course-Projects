package main.ast.node.statement;

import main.ast.node.expression.Expression;
import main.ast.type.Type;

import main.visitor.IVisitor;

public class ReturnStmt extends Statement {
    private Expression returnedExpr;

    private Type retType;
    public ReturnStmt(Expression returnedExpr) {
        this.returnedExpr = returnedExpr;
    }

    public void setRetType(Type retType) {
        this.retType = retType;
    }

    public Type getRetType() {
        return retType;
    }

    public Expression getReturnedExpr() {
        return returnedExpr;
    }

    public void setReturnedExpr(Expression returnedExpr) {
        this.returnedExpr = returnedExpr;
    }

    @Override
    public String toString() {
        return "ReturnStmt";
    }

    @Override
    public <T> T accept(IVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
