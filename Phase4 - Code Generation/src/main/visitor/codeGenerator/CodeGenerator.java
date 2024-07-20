package main.visitor.codeGenerator;
import main.ast.node.Program;
import java.util.*;
import main.ast.node.declaration.*;
import main.ast.node.expression.*;
import main.ast.node.expression.operators.BinaryOperator;
import main.ast.node.expression.operators.UnaryOperator;
import main.ast.node.expression.values.*;
import main.ast.type.complexType.OrderType;
import main.ast.type.complexType.TradeType;
import main.symbolTable.symbolTableItems.*;
import main.symbolTable.itemException.*;
import main.symbolTable.*;
import main.ast.node.statement.*;
import main.ast.type.Type;
import main.ast.type.primitiveType.*;
import main.visitor.Visitor;
import main.ast.node.declaration.FunctionDeclaration;
import main.visitor.typeAnalyzer.TypeChecker;

import java.util.ArrayList;
import java.io.*;
public class CodeGenerator extends Visitor<String> {
    TypeChecker expressionTypeChecker;
    private String generatedCode;
    private int label;
    private final HashMap<String, Integer> slots = new HashMap<>();
    private final List <String> fields ;
    private final String outputPath;
    private final Program program;
    private FileWriter currentFile;
    public CodeGenerator(Program _program) {
        this.expressionTypeChecker = new TypeChecker(new ArrayList<>());
        this.fields = new ArrayList<>();
        this.label = 0;
        this.generatedCode = "";
        this.outputPath = "output/";
        this.prepareOutputFolder();
        this.createFile();
        this.program = _program;
        this.visit(_program);
    }
    private Integer getSlot(String var){
        if (slots.containsKey(var))
            return slots.get(var);
        else{
            slots.put(var,slots.size());
            return slots.size()-1;
        }
    }
    private String newLabel() {
        String label = "Label_" + this.label;
        this.label++;
        return label;
    }
    private void prepareOutputFolder() {
        String jasminPath = "utilities/jarFiles/jasmin.jar";
        String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
        String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
        try{
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if(files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        }
        catch(SecurityException ignored) { }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
        copyFile(listClassPath, this.outputPath + "List.j");
        copyFile(fptrClassPath, this.outputPath + "Fptr.j");
    }
    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException ignored) {
        }
    }
    private void createFile(){
        try {
            String path = this.outputPath + "out" + ".j";
            File file = new File(path);
            file.createNewFile();
            this.currentFile = new FileWriter(path);
        } catch (IOException ignored) {}
    }

    private void addCommand(String command) {
        try {
            generatedCode = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                this.currentFile.write( command + "\n");
            else if(command.startsWith("."))
                this.currentFile.write(command + "\n");
            else {
                this.currentFile.write("\t\t" + generatedCode);
                if(!generatedCode.endsWith("\n"))
                    this.currentFile.write("\n");
            }
            this.currentFile.flush();
        } catch (IOException ignored) {}
    }

    private String createArgumentsSignature(Type t) {
        if (t instanceof IntType)
            return "Ljava/lang/Integer;";
        else if (t instanceof BoolType)
            return "Ljava/lang/Boolean;";
        else if (t instanceof StringType)
            return "Ljava/lang/String;";
        else if (t instanceof FloatType)
            return "Ljava/lang/Float;";
        else if (t instanceof TradeType)
            return "LTrade;";
        else if (t instanceof OrderType)
            return "LOrder;";
        else if (t instanceof VoidType)
            return "V";
        return " ";
    }
    private void varDecHelper(Type type, Expression defaultVal) {
        if (defaultVal != null)
            addCommand(defaultVal.accept(this));
        if (type instanceof IntType) {
            if (defaultVal == null)
                addCommand("ldc 0");
            addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
        }
        else if (type instanceof BoolType) {
            if (defaultVal == null)
                addCommand("ldc 0");
            addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
        }
        else if (type instanceof FloatType) {
            if (defaultVal == null)
                addCommand("ldc 0.0");
            addCommand("invokestatic java/lang/Float/valueOf(Z)Ljava/lang/Float;");
        }
    }
    public void createConstructor()
    {
        for (var dec : this.program.getVars()) {
            addCommand(".field public " + dec.getIdentifier().getName() + " " +createArgumentsSignature(dec.getType()));
        }
        addCommand(".method public <init>()V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload_0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        addCommand("areturn");
        addCommand(".end method");
    }

    @Override
    public String visit(Program program) {
        try {
            FunctionItem functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.START_KEY + "@Program");
            SymbolTable.push(functionItem.getFunctionSymbolTable());
        } catch (ItemNotFoundException ignored) {}
        createFile();
        addCommand(".class public UTL");
        addCommand(".super java/lang/Object");
        createConstructor();
        for(var onInit : program.getInits()) {
            slots.clear();
            onInit.accept(this);
        }
        for(var onStart : program.getStarts()) {
            slots.clear();
            onStart.accept(this);
        }
        for (var func : program.getFunctions()) {
            slots.clear();
            func.accept(this);
        }
        slots.clear();
        program.getMain().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(VarDeclaration varDeclaration) {
        int slot = getSlot(varDeclaration.getIdentifier().getName());
        Type varType = varDeclaration.getType();
        varDecHelper(varType, varDeclaration.getRValue());
        if(slot < 4)
            addCommand("astore_" + slot);
        else
            addCommand("astore " + slot);
        return null;
    }

    @Override
    public String visit(OnInitDeclaration onInitDeclaration) {
        try {
            OnInitItem onInitItem = (OnInitItem) SymbolTable.root.get(OnInitItem.START_KEY + onInitDeclaration.getTradeName().getName());
            SymbolTable.push(onInitItem.getOnInitSymbolTable());
        } catch (ItemNotFoundException ignored) {}
        StringBuilder commands = new StringBuilder(".method public ");
        commands.append("onInit(");
        commands.append(createArgumentsSignature(new TradeType())).append(")");
        getSlot(onInitDeclaration.getTradeName().getName());
        commands.append(createArgumentsSignature(new VoidType()));
        addCommand(commands.toString());
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        for(Statement stmt : onInitDeclaration.getBody())
            stmt.accept(this);
        addCommand(".end method");
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(OnStartDeclaration onStartDeclaration ) {
        try {
            OnStartItem onStartItem = (OnStartItem) SymbolTable.root.get(OnStartItem.START_KEY + onStartDeclaration.getTradeName().getName());
            SymbolTable.push(onStartItem.getOnStartSymbolTable());
        } catch (ItemNotFoundException ignored) {}
        StringBuilder commands = new StringBuilder(".method public ");
        commands.append("onStart(");
        commands.append(createArgumentsSignature(new TradeType())).append(")");
        getSlot(onStartDeclaration.getTradeName().getName());
        commands.append(createArgumentsSignature(new VoidType()));
        addCommand(commands.toString());
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        for(Statement stmt : onStartDeclaration.getBody())
            stmt.accept(this);
        addCommand(".end method");
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        try {
            FunctionItem functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.START_KEY + functionDeclaration.getName().getName());
            SymbolTable.push(functionItem.getFunctionSymbolTable());
        } catch (ItemNotFoundException ignored) {}
        StringBuilder commands = new StringBuilder(".method public ");
        commands.append(functionDeclaration.getName().getName()).append("(");
        for (VarDeclaration arg : functionDeclaration.getArgs()){
            commands.append(createArgumentsSignature(arg.getType()));
            getSlot(arg.getIdentifier().getName());
        }
        commands.append(")");
        commands.append(createArgumentsSignature(functionDeclaration.getReturnType()));
        addCommand(commands.toString());
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        for(Statement stmt : functionDeclaration.getBody())
        {
            if(stmt instanceof ReturnStmt)
                ((ReturnStmt) stmt).setRetType(functionDeclaration.getReturnType());
            stmt.accept(this);
        }
        addCommand(".end method");
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        try {
            MainItem mainItem = (MainItem) SymbolTable.root.get(MainItem.START_KEY + "main");
            SymbolTable.push(mainItem.getMainSymbolTable());
        } catch (ItemNotFoundException ignored) {}
        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("new UTL");
        addCommand("invokespecial UTL/<init>()V");
        addCommand("astore_0");
        getSlot("main");
        for(Statement stmt : mainDeclaration.getBody())
        {
            String temp = stmt.accept(this);
            if(temp != null)
                addCommand(temp);
        }
        addCommand("return");
        addCommand(".end method");
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(AssignStmt assignmentStmt) {
        Expression exp = new BinaryExpression(assignmentStmt.getLValue(),assignmentStmt.getRValue()
                , BinaryOperator.ASSIGN);
        addCommand(exp.accept(this));
        return null;
    }
    @Override
    public String visit(ExpressionStmt expressionStmt) {
        addCommand(expressionStmt.getExpression().accept(this));
        return null;
    }
    public String visitArgs(FunctionCall functionCall)
    {
        StringBuilder commands = new StringBuilder();
        for (var arg : functionCall.getArgs())
            commands.append(arg.accept(this));
        if (commands.toString() != "null")
            return commands.toString();
        else
            return " ";
    }

    @Override
    public String visit(IfElseStmt conditionalStmt) {
        String elseLabel = newLabel();
        String exitLabel = newLabel();
        addCommand(conditionalStmt.getCondition().accept(this));
        if (conditionalStmt.getElseBody().size() > 0)
            addCommand("ifeq " + elseLabel);
        for (Statement stmt : conditionalStmt.getThenBody())
            stmt.accept(this);
        addCommand("goto " + exitLabel);
        addCommand(elseLabel + ":");
        if(conditionalStmt.getElseBody() != null)
            for (Statement stmt : conditionalStmt.getElseBody())
                stmt.accept(this);
        addCommand(exitLabel + ":");
        return null;
    }

    @Override
    public String visit(WhileStmt whileStmt){
        String startLabel = newLabel ();
        String exitLabel = newLabel();
        addCommand(startLabel + ":");
        addCommand(whileStmt.getCondition().accept(this));
        addCommand("ifeq " + exitLabel);
        for (Statement stmt : whileStmt.getBody())
            stmt.accept(this);
        addCommand("goto " + startLabel);
        addCommand(exitLabel + ":");
        return null;
    }

    @Override
    public String visit(FunctionCall functionCall) {
        String commands = "";
        String name = functionCall.getFunctionName().getName();
        if(Objects.equals(name, "Connect")) {
            commands += visitArgs(functionCall);
            commands += "\ninvokestatic Prog/Connect(Ljava/lang/String;Ljava/lang/String;)V";
        }
        else if(Objects.equals(name, "Order")) {
            commands+= "\nnew Order\ndup ";
            commands += visitArgs(functionCall);
            commands += "\ninvokespecial Order/<init>(Ljava/lang/String;III)V";
        }
        else if(Objects.equals(name, "GetCandle")) {
            commands += visitArgs(functionCall);
            commands += "\ninvokestatic Prog/getCandle(Ljava/lang/Integer)LCandle";
        }
        else if(Objects.equals(name, "Observe")) {
            commands += visitArgs(functionCall);
            commands += "\ninvokestatic Prog/Observe(Ljava/lang/Integer;)Ltrade";
        }
        else if(Objects.equals(name, "Terminate")) {
            commands += visitArgs(functionCall);
            commands += "\ninvokestatic Prog/Terminate()V";
        }
        else if (Objects.equals(name, "Print")) {
            commands += functionCall.getArgs().get(0).accept(this);
            commands += "\ninvokevirtual java/io/PrintStream/println(Ljava/lang/String;)V";
        } else {

            commands += "\ninvokestatic UTL/" + name + "(";
            FunctionDeclaration currentFunction = null;
            for (FunctionDeclaration func : this.program.getFunctions()) {
                if (Objects.equals(func.getName().getName(), name))
                    currentFunction = func;
            }
            commands += visitArgs(functionCall);
            assert currentFunction != null;
            commands+= ")"+createArgumentsSignature(currentFunction.getReturnType());
        }
        return commands;
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        BinaryOperator operator = binaryExpression.getBinaryOperator();
        String commands = "";
        if (operator == BinaryOperator.PLUS) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\niadd";
        }
        else if (operator == BinaryOperator.MINUS) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nisub";
        }
        else if (operator == BinaryOperator.MULT) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nimul";
        }
        else if (operator == BinaryOperator.DIV) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nidiv";
        }
        else if (operator == BinaryOperator.MOD) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nimod";
        }
        else if (operator == BinaryOperator.BIT_OR) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nior";
        }
        else if (operator == BinaryOperator.BIT_AND) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\niand";
        }
        else if (operator == BinaryOperator.BIT_XOR) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nixor";
        }
        else if((operator == BinaryOperator.GT) || (operator == BinaryOperator.LT)) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
            String trueLabel = newLabel();
            String afterLabel = newLabel();
            if (operator == BinaryOperator.GT)
                commands += "\nif_icmpgt " + trueLabel;
            else
                commands += "\nif_icmplt " + trueLabel;
            commands += "\nldc 0";
            commands += "\ngoto " + afterLabel;
            commands += "\n" + trueLabel + ":";
            commands += "\nldc 1";
            commands += "\n" + afterLabel + ":";
        }
        else if((operator == BinaryOperator.EQ) || (operator == BinaryOperator.NEQ) ) {
            commands += binaryExpression.getLeft().accept(this);
            commands += "\n" + binaryExpression.getRight().accept(this);
//            System.out.println(commands);
            String trueLabel = newLabel();
            String afterLabel = newLabel();
            if (operator == BinaryOperator.EQ)
                commands += "\nif_icmpeq "  + trueLabel ;
            else
                commands += "\nif_icmpne "  + trueLabel ;
            commands += "\nldc 0";
            commands += "\ngoto " + afterLabel;
            commands += "\n" + trueLabel + ":";
            commands += "\nldc 1";
            commands += "\n" + afterLabel + ":";
        }
        else if(operator == BinaryOperator.AND) {
            String shortCircuitLabel = newLabel();
            String trueLabel = newLabel();
            String afterLabel = newLabel();
            commands += binaryExpression.getLeft().accept(this);
            commands += "\nifeq " + shortCircuitLabel;
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nifne " + trueLabel;
            commands += "\n" + shortCircuitLabel + ":";
            commands += "\nldc 0";
            commands += "\ngoto " + afterLabel;
            commands += "\n" + trueLabel + ":";
            commands += "\nldc 1";
            commands += "\n" + afterLabel + ":";
        }
        else if(operator == BinaryOperator.OR) {
            String trueLabel = newLabel();
            String afterLabel = newLabel();
            commands = "; logical OR\n";
            commands += binaryExpression.getLeft().accept(this);
            commands += "\nifne " + trueLabel;
            commands += "\n" + binaryExpression.getRight().accept(this);
            commands += "\nifne " + trueLabel;
            commands += "\nldc 0";
            commands += "\ngoto " + afterLabel;
            commands += "\n" + trueLabel + ":";
            commands += "\nldc 1";
            commands += "\n" + afterLabel + ":";
        }
        else if(operator == BinaryOperator.ASSIGN || operator == BinaryOperator.ADD_ASSIGN
                || operator == BinaryOperator.DIV_ASSIGN || operator == BinaryOperator.MOD_ASSIGN
                || operator == BinaryOperator.MUL_ASSIGN || operator == BinaryOperator.SUB_ASSIGN) {
            commands = "";
            if(operator != BinaryOperator.ASSIGN)
                commands += binaryExpression.getLeft().accept(this);
            commands += binaryExpression.getRight().accept(this);
            switch (operator) {
                case ADD_ASSIGN -> commands += "\niadd";
                case DIV_ASSIGN -> commands += "\nidiv";
                case MOD_ASSIGN -> commands += "\nimod";
                case MUL_ASSIGN -> commands += "\nimul";
                case SUB_ASSIGN -> commands += "\nisub";
                default -> {
                }
            }
            if(binaryExpression.getLeft() instanceof Identifier &&!(binaryExpression.getLeft() instanceof ArrayIdentifier) ) {
                String name = ((Identifier) binaryExpression.getLeft()).getName();
                if(fields.contains(name))
                    commands += "putfield UTL/" + name + " " + createArgumentsSignature(binaryExpression.getLeft().getType());
                else
                    commands += "\nistore " + getSlot(name);
            }
            else if(binaryExpression.getLeft() instanceof ArrayIdentifier) {
                String name = ((ArrayIdentifier) binaryExpression.getLeft()).getName();
                if(fields.contains(name))
                    commands += "putfield UTL/" + name + " " + createArgumentsSignature(binaryExpression.getLeft().getType());
                else
                    commands += "\nastore " + getSlot(name);
            }
        }
        return commands;
    }

    @Override
    public String visit(UnaryExpression unaryExpression){
        UnaryOperator operator = unaryExpression.getUnaryOperator();
        String commands = "";
        Identifier id = (Identifier) unaryExpression.getOperand() ;
        int slot = getSlot(id.getName());
        if(fields.contains(id.getName())) {
            commands += "getfield UTL/" + id.getName() +" " + createArgumentsSignature(id.getType());
        }
        else{
            if(slot > 3)
                commands += "\niload " + slot ;
            else
                commands += "\niload_" + slot;
        }
        if (operator == UnaryOperator.DEC) {
            commands += "\nldc 1";
            commands += "\nisub";
        }
        else if (operator == UnaryOperator.INC) {
            commands += "\nldc 1";
            commands += "\niadd";
        }
        else if (operator == UnaryOperator.MINUS) {
            commands += "\nineg";
        }
        if(fields.contains(id.getName())) {
            commands += "putfield UTL/" + id.getName() +" " + createArgumentsSignature(id.getType());
        }
        else {
            if (slot > 3)
                commands += "\nistore " + slot;
            else
                commands += "\nistore_" + slot;
        }
        return commands;
    }

    @Override
    public String visit(Identifier identifier){
        try {
            SymbolTable.root.get(FunctionItem.START_KEY + identifier.getName());
        }
        catch (ItemNotFoundException ignored){}
        String commands = "";
        if(fields.contains(identifier.getName()))
            commands += "getfield UTL/" + identifier.getName() + " " + createArgumentsSignature(identifier.getType());
        else {
            String start = "a";
            if(identifier.getType() instanceof BoolType || identifier.getType() instanceof IntType)
                start = "i";
            int slot = getSlot(identifier.getName());
            if (slot > 3)
                commands += "\n" + start + "load " + slot;
            else
                commands += "\n" + start + "load_" + slot;
        }
        return commands;
    }

    @Override
    public String visit(ArrayIdentifier arrIdentifier){
        String commands = "";
        Identifier id  = new Identifier(arrIdentifier.getName());
        if(fields.contains(id.getName())) {
            commands += "getfield UTL/" + id.getName() +" " + createArgumentsSignature(id.getType());
        }
        else {
            int slot = getSlot(id.getName());
            if (slot > 3)
                commands += "\naload " + slot;
            else
                commands += "\naload_" + slot;
        }
        commands += arrIdentifier.getIndex().accept(this);
        return commands;
    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        addCommand(returnStmt.getReturnedExpr().accept(this));
        Type type = returnStmt.getRetType();
        if(type instanceof NullType || type instanceof VoidType)
            addCommand("return");
        else if (type instanceof IntType || type instanceof BoolType)
            addCommand("ireturn");
        else
            addCommand("areturn");
        return null;
    }

    @Override
    public String visit(NullValue nullValue) {
        return "ldc 0";
    }

    @Override
    public String visit(IntValue intValue) {
        int val = intValue.getConstant();
        return "\nldc " + val;
    }

    @Override
    public String visit(FloatValue floatValue) {
        float val = floatValue.getConstant();
        return "\nldc " + val;
    }

    @Override
    public String visit(BoolValue boolValue) {
        boolean val = boolValue.getConstant();
        if(val)
            return "\nldc 1";
        else
            return "\nldc 0";
    }

    @Override
    public String visit(StringValue stringValue) {
        String val = stringValue.getConstant();
        return "ldc " + val ;
    }
}