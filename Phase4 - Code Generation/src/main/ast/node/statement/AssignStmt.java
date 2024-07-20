package main.ast.node.statement;

import main.ast.node.expression.Expression;
import main.ast.node.expression.operators.BinaryOperator;
import main.visitor.IVisitor;

public class AssignStmt extends Statement {
    private Expression lValue;
    private Expression rValue;

    private BinaryOperator Op;

    public AssignStmt(Expression lValue, Expression rValue) {
        this.lValue = lValue;
        this.rValue = rValue;
    }

    public void setOp(String op) {
        switch (op) {
            case "=" -> Op = BinaryOperator.ASSIGN;
            case "+=" -> Op = BinaryOperator.ADD_ASSIGN;
            case "-=" -> Op = BinaryOperator.SUB_ASSIGN;
            case "/=" -> Op = BinaryOperator.DIV_ASSIGN;
            case "%=" -> Op = BinaryOperator.MOD_ASSIGN;
            case "*=" -> Op = BinaryOperator.MUL_ASSIGN;
            default -> {}
        }
    }
    public BinaryOperator getOp() {return this.Op;}

    public Expression getLValue() {
        return lValue;
    }

    public void setLValue(Expression lValue) {
        this.lValue = lValue;
    }

    public Expression getRValue() {
        return rValue;
    }

    public void setRValue(Expression rValue) {
        this.rValue = rValue;
    }

    @Override
    public String toString() {
        return "AssignStmt";
    }

    @Override
    public <T> T accept(IVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
