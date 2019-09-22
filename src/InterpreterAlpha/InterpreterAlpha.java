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
        if (node instanceof Program)
            return visit_Program((Program) node);
        else if (node instanceof BinOp)
            return visit_BinOp((BinOp) node);
        else if (node instanceof Num)
            return visit_Num((Num) node);
        else if (node instanceof UnaryOp)
            return visit_UnaryOp((UnaryOp) node);
        else if (node instanceof NoOp)
            return visit_NoOp((NoOp) node);
        else if (node instanceof Assign)
            return visit_Assign((Assign) node);
        else if (node instanceof Var)
            return visit_Var((Var) node);
        else if (node instanceof ForStatement)
            return visit_for((ForStatement) node);
        else
            throw new Exception("No visit function for the type of node!");
    }

    String visit_for(ForStatement node) throws Exception {
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

    String visit_NoOp(NoOp node) throws Exception {
        return "";
    }

    String visit_Assign(Assign node) throws Exception {
        return "";
    }

    String visit_Var(Var node) throws Exception {
        return "";
    }
}

//INTERPRETER

public class InterpreterAlpha extends NodeVisitor {
    // store table of (var_name; var_value)
    Map<String, String> GLOBAL_SCOPE;
    AST tree;
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
            InterpreterAlpha.resultFile.append(key + " = " + scope.get(key) + "\n");
    }

    @Override
    String visit_Program(Program node) throws Exception {
        for (AST child : node.statement_list)
            this.visit(child);
        return "";
    }

    @Override
    String visit_for(ForStatement node) throws Exception {
        String val = GLOBAL_SCOPE.getOrDefault(node.variable.value, null);
        if (val == null)
            throw new Exception("No variable found in for_loop!");
        else {
            int end_point = Integer.parseInt(val);
            for (int tmp = 1; tmp <= end_point; tmp++) {
                for (AST nod : node.statements)
                    this.visit(nod);
            }
        }
        return "";
    }

    @Override
    String visit_BinOp(BinOp node) throws Exception {
        switch (node.op.type) {
            case PLUS:
                return Integer.toString(Integer.parseInt(this.visit(node.left)) + Integer.parseInt(this.visit(node.right)));
            case MINUS:
                return Integer.toString(Integer.parseInt(this.visit(node.left)) - Integer.parseInt(this.visit(node.right)));
            case MUL:
                return Integer.toString(Integer.parseInt(this.visit(node.left)) * Integer.parseInt(this.visit(node.right)));
            case DIV:
                return Integer.toString(Integer.parseInt(this.visit(node.left)) / Integer.parseInt(this.visit(node.right)));
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
                return Integer.toString(+Integer.parseInt(this.visit(((UnaryOp) node).expr)));
            case MINUS:
                return Integer.toString(-Integer.parseInt(this.visit(((UnaryOp) node).expr)));
            default:
                throw new Exception("Incorrect unary operator!");
        }
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

        InterpreterAlpha interpreter = new InterpreterAlpha(tree);
        interpreter.interpret();
        InterpreterAlpha.resultFile.close();
    }
}