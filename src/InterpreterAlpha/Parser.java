package InterpreterAlpha;

import java.util.ArrayList;

// a node on the Abstract Syntax Tree
class AST {
}

class Program extends AST {
    ArrayList<AST> statement_list;

    Program(ArrayList<AST> statement_list) {
        this.statement_list = statement_list;
    }
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
    ArrayList<AST> statements;
    Var variable;

    ForStatement(ArrayList<AST> statements, Var variable) {
        this.statements = statements;
        this.variable = variable;
    }
}

// empty operator
class NoOp extends AST {

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


    // statement: for_statement
    //          | assign_statement
    //          | empty
    AST statement() throws Exception {
        AST node;
        if (this.current_token.type == TokenType.ID)
            node = this.assign_statement();
        else if (this.current_token.type == TokenType.LOOP)
            node = this.for_statement();
        else
            node = this.empty();
        return node;
    }

    // for_statement: LOOP ID DO statement_list END
    ForStatement for_statement() throws Exception {
        this.eat(TokenType.LOOP);
        Var variable = this.variable();
        this.eat(TokenType.DO);
        ArrayList<AST> statements = this.statement_list();
        this.eat(TokenType.END);
        return new ForStatement(statements, variable);
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

        while (this.current_token.type == TokenType.DIV
                || this.current_token.type == TokenType.MUL) {
            Token token = this.current_token;
            if (token.type == TokenType.MUL) {
                this.eat(TokenType.MUL);
            } else
                this.eat(TokenType.DIV);

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
        ArrayList<AST> nodes = this.statement_list();
        if (this.current_token.type != TokenType.EOF)
            this.error();
        return new Program(nodes);
    }
}