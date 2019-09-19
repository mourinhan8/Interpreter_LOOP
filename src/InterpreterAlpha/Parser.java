package InterpreterAlpha;

import java.util.ArrayList;

// a node on the Abstract Syntax Tree
class AST {
}

class BinOp extends AST {
    AST left;
    AST right;
    Token op;

    BinOp(AST left, Token op, AST right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

class UnaryOp extends AST {
    Token op;
    AST expr;

    UnaryOp(Token op, AST expr) {
        this.op = op;
        this.expr = expr;
    }
}

class Num extends AST {
    Token token;
    String value;

    Num(Token token) {
        this.token = token;
        this.value = token.value;
    }
}

class Compound extends AST {
    ArrayList<AST> children;

    Compound() {
        this.children = new ArrayList<AST>();
    }
}

class Assign extends AST {
    Var left;
    Token op;
    AST right;

    Assign(Var left, Token op, AST right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

class Var extends AST {
    Token token;
    String value; // variable's name

    Var(Token token) {
        this.token = token;
        this.value = token.value;
    }
}

class ForStatement extends AST {
    Compound compound_statement;
    int end_point;
    int start_point;
    Var variable;

    ForStatement(Compound compound_statement, int end_point, int start_point, Var variable) {
        this.compound_statement = compound_statement;
        this.end_point = end_point;
        this.start_point = start_point;
        this.variable = variable;
    }
}

// empty operator
class NoOp extends AST {

}

class Program extends AST {
    String name;
    Block block;

    Program(String name, Block block) {
        this.name = name;
        this.block = block;
    }
}

class Block extends AST {
    ArrayList<VarDecl> declaration;
    Compound compound_statement;

    Block(ArrayList<VarDecl> declaration, Compound compound_statement) {
        this.declaration = declaration;
        this.compound_statement = compound_statement;
    }
}

class VarDecl extends AST {
    Var var_node;
    Type type_node;

    VarDecl(Var var_node, Type type_node) {
        this.var_node = var_node;
        this.type_node = type_node;
    }
}

// an integer or a real number
class Type extends AST {
    Token token;
    String value;

    Type(Token token) {
        this.token = token;
        this.value = token.value;
    }
}

class Parser {
    Lexer lexer;
    Token current_token;

    Parser(Lexer lexer) throws Exception {
        this.lexer = lexer;
        this.current_token = this.lexer.get_next_token();
    }

    void error() throws Exception {
        throw new Exception("Invalid syntax!");
    }

    void eat(TokenType token_type) throws Exception {
        if (this.current_token.type == token_type) {
            this.current_token = this.lexer.get_next_token();
        } else {
            this.error();
        }
    }

    // program: PROGRAM variable SEMI block DOT
    Program program() throws Exception {
        this.eat(TokenType.PROGRAM);
        Var var_node = this.variable();
        String program_name = var_node.value;
        this.eat(TokenType.SEMI);
        Block block_node = this.block();
        this.eat(TokenType.DOT);
        return new Program(program_name, block_node);
    }

    // block: declarations compound_statement
    Block block() throws Exception {
        ArrayList<VarDecl> declaration_nodes = this.declarations();
        Compound compound_statement_node = this.compound_statement();
        return new Block(declaration_nodes, compound_statement_node);
    }

    // declarations: VAR (variable_declaration SEMI)*
    //              | empty
    ArrayList<VarDecl> declarations() throws Exception {
        ArrayList<VarDecl> declarations = new ArrayList<VarDecl>();
        if (this.current_token.type == TokenType.VAR) {
            this.eat(TokenType.VAR);
            while (this.current_token.type == TokenType.ID) {
                ArrayList<VarDecl> var_decl = this.variable_declaration();
                declarations.addAll(var_decl);
                this.eat(TokenType.SEMI);
            }
        }
        return declarations;
    }

    // variable_declaration: ID (COMMA ID)* COLON type_spec
    ArrayList<VarDecl> variable_declaration() throws Exception {
        ArrayList<Var> var_nodes = new ArrayList<Var>();
        var_nodes.add(new Var(this.current_token));
        this.eat(TokenType.ID);

        while (this.current_token.type == TokenType.COMMA) {
            this.eat(TokenType.COMMA);
            var_nodes.add(new Var(this.current_token));
            this.eat(TokenType.ID);
        }

        this.eat(TokenType.COLON);
        Type type_node = this.type_spec();
        ArrayList<VarDecl> var_declarations = new ArrayList<VarDecl>();
        for (Var var_node : var_nodes)
            var_declarations.add(new VarDecl(var_node, type_node));
        return var_declarations;
    }

    // type_spec: INTEGER | REAL
    Type type_spec() throws Exception {
        Token token = this.current_token;
        if (this.current_token.type == TokenType.INTEGER)
            this.eat(TokenType.INTEGER);
        else
            this.eat(TokenType.REAL);
        return new Type(token);
    }

    // compound_statement: BEGIN statement_list END
    Compound compound_statement() throws Exception {
        this.eat(TokenType.BEGIN);
        ArrayList<AST> nodes = this.statement_list();
        this.eat(TokenType.END);

        Compound root = new Compound();
        root.children.addAll(nodes);
        return root;
    }

    // statement_list: statement
    //               | statement SEMI statement_list
    ArrayList<AST> statement_list() throws Exception {
        AST node = this.statement();
        ArrayList<AST> results = new ArrayList<AST>();
        results.add(node);
        while (this.current_token.type == TokenType.SEMI) {
            this.eat(TokenType.SEMI);
            results.add(this.statement());
        }

        if (this.current_token.type == TokenType.ID)
            this.error();
        return results;
    }

    // statement: compound_statement
    //          | for_statement
    //          | assign_statement
    //          | empty
    AST statement() throws Exception {
        AST node;
        if (this.current_token.type == TokenType.BEGIN)
            node = this.compound_statement();
        else if (this.current_token.type == TokenType.ID)
            node = this.assign_statement();
        else if (this.current_token.type == TokenType.FOR)
            node = this.for_statement();
        else
            node = this.empty();
        return node;
    }

    // for_statement: FOR ID := INTEGER_CONST TO INTEGER_CONST DO compound_statement
    ForStatement for_statement() throws Exception {
        this.eat(TokenType.FOR);
        Var variable = this.variable();
        this.eat(TokenType.ASSIGN);
        Token start = this.current_token; // get the start point
        this.eat(TokenType.INTEGER_CONST);
        this.eat(TokenType.TO);
        Token end = this.current_token; // get the end point
        this.eat(TokenType.INTEGER_CONST);
        this.eat(TokenType.DO);
        Compound compound = this.compound_statement();
        return new ForStatement(compound, Integer.parseInt(end.value), Integer.parseInt(start.value), variable);
    }

    // assign_statement: variable ASSIGN expr
    Assign assign_statement() throws Exception {
        Var left = this.variable();
        Token token = this.current_token;
        this.eat(TokenType.ASSIGN);
        AST right = this.expr();
        return new Assign(left, token, right);
    }

    // variable: ID
    Var variable() throws Exception {
        Var node = new Var(this.current_token);
        this.eat(TokenType.ID);
        return node;
    }

    NoOp empty() {
        return new NoOp();
    }

    // factor: PLUS factor
    //         | MINUS factor
    //         | INTEGER_CONST
    //         | REAL_CONST
    //         | LPAREN expr RPAREN
    //         | variable
    AST factor() throws Exception {
        Token token = this.current_token;
        switch (token.type) {
            case PLUS:
                this.eat(TokenType.PLUS);
                return new UnaryOp(token, this.factor());
            case MINUS:
                this.eat(TokenType.MINUS);
                return new UnaryOp(token, this.factor());
            case INTEGER_CONST:
                this.eat(TokenType.INTEGER_CONST);
                return new Num(token);
            case REAL_CONST:
                this.eat(TokenType.REAL_CONST);
                return new Num(token);
            case LPAREN:
                this.eat(TokenType.LPAREN);
                AST node = this.expr();
                this.eat(TokenType.RPAREN);
                return node;
            default:
                return this.variable();
        }
    }

    // term: factor ((MUL | DIV) factor)*
    AST term() throws Exception {
        AST node = this.factor();

        while (this.current_token.type == TokenType.FLOAT_DIV
                || this.current_token.type == TokenType.INTEGER_DIV
                || this.current_token.type == TokenType.MUL) {
            Token token = this.current_token;
            if (token.type == TokenType.MUL) {
                this.eat(TokenType.MUL);
            } else if (token.type == TokenType.INTEGER_DIV)
                this.eat(TokenType.INTEGER_DIV);
            else
                this.eat(TokenType.FLOAT_DIV);

            node = new BinOp(node, token, this.factor());
        }
        return node;
    }

    // expr: term ((PLUS | MINUS) term)*
    AST expr() throws Exception {
        AST node = this.term();

        while (this.current_token.type == TokenType.MINUS || this.current_token.type == TokenType.PLUS) {
            Token token = this.current_token;
            if (token.type == TokenType.MINUS) {
                this.eat(TokenType.MINUS);
            } else
                this.eat(TokenType.PLUS);

            node = new BinOp(node, token, this.term());
        }
        return node;
    }

    AST parse() throws Exception {
        AST node = this.program();
        if (this.current_token.type != TokenType.EOF)
            this.error();
        return node;
    }
}
