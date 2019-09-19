package InterpreterAlpha;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

//AST visitors
class NodeVisitor {
    String visit(AST node) throws Exception {
        if (node instanceof Block)
            return visit_Block((Block) node);
        else if (node instanceof Program)
            return visit_Program((Program) node);
        else if (node instanceof BinOp)
            return visit_BinOp((BinOp) node);
        else if (node instanceof Num)
            return visit_Num((Num) node);
        else if (node instanceof UnaryOp)
            return visit_UnaryOp((UnaryOp) node);
        else if (node instanceof Compound)
            return visit_Compound((Compound) node);
        else if (node instanceof NoOp)
            return visit_NoOp((NoOp) node);
        else if (node instanceof VarDecl)
            return visit_VarDecl((VarDecl) node);
        else if (node instanceof Assign)
            return visit_Assign((Assign) node);
        else if (node instanceof Var)
            return visit_Var((Var) node);
        else if (node instanceof Type)
            return visit_Type((Type) node);
        else if (node instanceof ForStatement)
            return visit_for((ForStatement) node);
        else
            throw new Exception("No visit function for the type of node!");
    }

    String visit_Type(Type node) throws Exception {
        return "";
    }

    String visit_for(ForStatement node) throws Exception {
        return "";
    }

    String visit_Block(Block node) throws Exception {
        return "";
    }

    String visit_Program(Program node) throws Exception {
        return "";
    }

    String visit_BinOp(BinOp node) throws Exception {
        return "";
    }

    String visit_Num(Num node) throws Exception {
        return "";
    }

    String visit_UnaryOp(UnaryOp node) throws Exception {
        return "";
    }

    String visit_Compound(Compound node) throws Exception {
        return "";
    }

    String visit_NoOp(NoOp node) throws Exception {
        return "";
    }

    String visit_VarDecl(VarDecl node) throws Exception {
        return "";
    }

    String visit_Assign(Assign node) throws Exception {
        return "";
    }

    String visit_Var(Var node) throws Exception {
        return "";
    }
}

//SYMBOL and SYMBOL TABLE
class Symbol {
    String name;
    TokenType type; // INTEGER | REAL
    static public FileWriter resultFile;

    static {
        try {
            FileWriter deletedFile = new FileWriter("result.txt", false);
            deletedFile.write("");
            deletedFile.close();
            resultFile = new FileWriter("result.txt", true);
        } catch (IOException e) {
            System.out.println("File doesn't exist!");
        }
    }

    // built-in type
    Symbol(String name) {
        this.name = name;
        if (name.equals("INTEGER"))
            this.type = TokenType.INTEGER;
        else
            this.type = TokenType.REAL;
    }

    // variable
    Symbol(String name, TokenType type) {
        this.name = name;
        this.type = type;
    }

    void printSym() throws Exception {
    }
}

class VarSymbol extends Symbol {
    VarSymbol(String name, TokenType type) {
        super(name, type);
    }

    @Override
    void printSym() throws Exception {
        Symbol.resultFile.append("<" + this.name + " : " + this.type + ">");
    }
}

class BuiltinTypeSymbol extends Symbol {
    BuiltinTypeSymbol(String name) {
        super(name);
    }

    @Override
    void printSym() throws Exception {
        Symbol.resultFile.append(this.name);
    }
}

// map of (symbol_name, symbol)
class SymbolTable {
    Map<String, Symbol> _symbols;

    SymbolTable() {
        this._symbols = new HashMap<>();
        this.initBuiltins();
    }

    private void initBuiltins() {
        this.define(new BuiltinTypeSymbol("INTEGER"));
        this.define(new BuiltinTypeSymbol("REAL"));
    }

    void define(Symbol symbol) {
        this._symbols.put(symbol.name, symbol);
    }

    // return an instance of Symbol class or a null objects
    Symbol lookup(String name) {
        return this._symbols.get(name);
    }
}

class SymbolTableBuilder extends NodeVisitor {
    SymbolTable symtab;

    SymbolTableBuilder() {
        this.symtab = new SymbolTable();
    }

    void printTable() throws Exception {
        for (String name : symtab._symbols.keySet()) {
            symtab._symbols.get(name).printSym();
            Symbol.resultFile.append("; ");
        }
        Symbol.resultFile.append("\n\n");
    }

    @Override
    String visit_Block(Block node) throws Exception {
        for (VarDecl declaration : node.declaration)
            this.visit(declaration);
        this.visit(node.compound_statement);
        return "";
    }

    @Override
    String visit_Program(Program node) throws Exception {
        this.visit(node.block);
        return "";
    }

    @Override
    String visit_BinOp(BinOp node) throws Exception {
        this.visit(node.left);
        this.visit(node.right);
        return "";
    }

    @Override
    String visit_UnaryOp(UnaryOp node) throws Exception {
        this.visit(node.expr);
        return "";
    }

    @Override
    String visit_Compound(Compound node) throws Exception {
        for (AST child : node.children)
            this.visit(child);
        return "";
    }

    @Override
    String visit_VarDecl(VarDecl node) throws Exception {
        String type_name = node.type_node.value;
        Symbol type_symbol = this.symtab.lookup(type_name);
        String var_name = node.var_node.value;
        VarSymbol var_symbol = new VarSymbol(var_name, type_symbol.type);
        this.symtab.define(var_symbol);
        return "";
    }

    @Override
    String visit_Assign(Assign node) throws Exception {
        String var_name = node.left.value;
        Symbol var_symbol = this.symtab.lookup(var_name);
        if (var_symbol == null)
            throw new Exception("No var_name declaration for " + var_name);
        this.visit(node.right);
        return "";
    }

    @Override
    String visit_Var(Var node) throws Exception {
        String var_name = node.value;
        Symbol var_symbol = this.symtab.lookup(var_name);
        if (var_symbol == null)
            throw new Exception("No var_name declaration for " + var_name);
        return "";
    }
}

//INTERPRETER

public class InterpreterAlpha extends NodeVisitor {
    // store table of (var_name; var_value)
    Map<String, String> GLOBAL_SCOPE;
    AST tree;

    InterpreterAlpha(AST tree) {
        this.tree = tree;
        GLOBAL_SCOPE = new HashMap<>();
    }

    void interpret() throws Exception {
        AST tree = this.tree;
        if (tree == null) {
            throw new Exception("Have no tree to trace!");
        }
        this.visit(tree);
        printTab(GLOBAL_SCOPE);
    }

    void printTab(Map<String, String> scope) throws Exception {
        for (String key : scope.keySet())
            Symbol.resultFile.append(key + " = " + scope.get(key) + "\n");
    }

    @Override
    String visit_Program(Program node) throws Exception {
        return this.visit(node.block);
    }

    @Override
    String visit_for(ForStatement node) throws Exception {
        GLOBAL_SCOPE.put(node.variable.value, null);
        for (int tmp = node.start_point; tmp <= node.end_point; tmp++) {
            this.visit(node.compound_statement);
            GLOBAL_SCOPE.replace(node.variable.value, Integer.toString(tmp));
        }
        return "";
    }

    @Override
    String visit_Block(Block node) throws Exception {
        for (AST declaration : node.declaration)
            this.visit(declaration);
        this.visit(node.compound_statement);
        return "";
    }

    @Override
    String visit_BinOp(BinOp node) throws Exception {
        switch (node.op.type) {
            case PLUS:
                return Double.toString(Double.parseDouble(this.visit(node.left)) + Double.parseDouble(this.visit(node.right)));
            case MINUS:
                return Double.toString(Double.parseDouble(this.visit(node.left)) - Double.parseDouble(this.visit(node.right)));
            case MUL:
                return Double.toString(Double.parseDouble(this.visit(node.left)) * Double.parseDouble(this.visit(node.right)));
            case FLOAT_DIV:
                return Double.toString(Double.parseDouble(this.visit(node.left)) / Double.parseDouble(this.visit(node.right)));
            case INTEGER_DIV:
                return Integer.toString((int) Double.parseDouble(this.visit(node.left)) / (int) Double.parseDouble(this.visit(node.right)));
            default:
                throw new Exception("Incorrect binary operator!");
        }
    }

    @Override
    String visit_Num(Num node) throws Exception {
        return node.value;
    }

    @Override
    String visit_UnaryOp(UnaryOp node) throws Exception {
        switch (node.op.type) {
            case PLUS:
                return Double.toString(+Double.parseDouble(this.visit(((UnaryOp) node).expr)));
            case MINUS:
                return Double.toString(-Double.parseDouble(this.visit(((UnaryOp) node).expr)));
            default:
                throw new Exception("Incorrect unary operator!");
        }
    }

    @Override
    String visit_Compound(Compound node) throws Exception {
        for (AST child : node.children)
            this.visit(child);
        return "";
    }

    @Override
    String visit_Assign(Assign node) throws Exception {
        String var_name = node.left.value;
        GLOBAL_SCOPE.put(var_name, this.visit(node.right));
        return "";
    }

    @Override
    String visit_Var(Var node) throws Exception {
        String var_name = node.value;
        String val = this.GLOBAL_SCOPE.get(var_name);
        if (val != null)
            return val;
        else
            throw new Exception("Variable has no value!");
    }

    public static void main(String[] args) throws Exception {
        String str = Files.readString(Paths.get("Program.txt"));
        if (str.equals("")) {
            throw new Exception("Please input text into Program.txt");
        }
        Lexer lexer = new Lexer(str);
        Parser parser = new Parser(lexer);
        AST tree = parser.parse();
        SymbolTableBuilder symtab = new SymbolTableBuilder();
        symtab.visit(tree);
        symtab.printTable();

        InterpreterAlpha interpreter = new InterpreterAlpha(tree);
        interpreter.interpret();
        Symbol.resultFile.close();
    }
}
