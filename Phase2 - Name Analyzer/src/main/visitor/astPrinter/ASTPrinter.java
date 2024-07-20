package main.visitor.astPrinter;

import main.ast.node.Program;
import main.ast.node.declaration.FunctionDeclaration;
import main.ast.node.declaration.OnInitDeclaration;
import main.ast.node.declaration.OnStartDeclaration;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.statement.Statement;
import main.visitor.Visitor;

public class ASTPrinter extends Visitor<Void> {
    public void messagePrinter(int line, String message){
        System.out.println("Line:" + line + ":" + message);
    }

    @Override
    public Void visit(Program program) {
        messagePrinter(program.getLine(), program.toString());
        for (VarDeclaration varDeclaration : program.getVars())
            varDeclaration.accept(this);
        for (FunctionDeclaration functionDeclaration : program.getFunctions())
            functionDeclaration.accept(this);
        for (OnInitDeclaration onInitDeclaration : program.getInits())
            onInitDeclaration.accept(this);
        for (OnStartDeclaration onStartDeclaration : program.getStarts())
            onStartDeclaration.accept(this);
        program.getMain().accept(this);
        return null;
    }

    //TODO: implement other visit methods

}

