package main.visitor.nameAnalyzer;

import main.ast.node.Program;
import main.ast.node.declaration.*;
import main.ast.node.statement.Statement;
import main.compileError.CompileError;
import main.symbolTable.SymbolTable;
import main.symbolTable.symbolTableItems.OnInitItem;
import main.visitor.Visitor;

import java.util.ArrayList;

public class NameAnalyzer extends Visitor<Void> {

    public ArrayList<CompileError> nameErrors = new ArrayList<>();

    @Override
    public Void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        SymbolTable.push(SymbolTable.root);

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

    @Override
    public Void visit(OnInitDeclaration onInitDeclaration) {
        OnInitItem onInitItem = new OnInitItem(onInitDeclaration);
        SymbolTable onInitSymbolTable = new SymbolTable(SymbolTable.top, onInitDeclaration.getTradeName().getName());
        onInitItem.setOnInitSymbolTable(onInitSymbolTable);

        // TODO check the onInit name is redundant or not , if it is redundant change its name and put it

        // TODO push onInit symbol table

        // TODO visit statements

        // TODO pop onInit symbol table

        return null;
    }

    @Override
    public Void visit(OnStartDeclaration onStartDeclaration) {
        // TODO

        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        // TODO

        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDeclaration) {
        // TODO

        return null;
    }

    @Override
    public Void visit(VarDeclaration varDeclaration) {
        // TODO

        return null;
    }
}

