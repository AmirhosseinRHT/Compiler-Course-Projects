package main.symbolTable.symbolTableItems;

import main.ast.node.declaration.OnInitDeclaration;
import main.symbolTable.SymbolTable;

public class OnStartItem extends SymbolTableItem {

    protected SymbolTable onStartSymbolTable;
    protected OnInitDeclaration onInitDeclaration;
    public static final String START_KEY = "OnStart_";

    public OnStartItem(OnInitDeclaration onInitDeclaration)
    {
        this.name = onInitDeclaration.getTradeName().getName();
        this.onInitDeclaration = onInitDeclaration;
    }
    
    public void setOnStartSymbolTable(SymbolTable onStartSymbolTable)
    {
        this.onStartSymbolTable = onStartSymbolTable;
    }

    public SymbolTable getOnStartSymbolTable()
    {
        return onStartSymbolTable;
    }

    public void setName(String name)
    {
        this.name = name;
        this.onInitDeclaration.getTradeName().setName(name);
    }

    public void setActorDeclaration(OnInitDeclaration onInitDeclaration)
    {
        this.onInitDeclaration = onInitDeclaration;
    }

    public OnInitDeclaration getActorDeclaration()
    {
        return onInitDeclaration;
    }

    @Override
    public String getKey()
    {
        return OnStartItem.START_KEY + this.name;
    }
}
