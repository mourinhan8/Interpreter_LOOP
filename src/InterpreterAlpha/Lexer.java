package InterpreterAlpha;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

enum TokenType {
    INTEGER,
    INTEGER_CONST,
    PLUS, // +
    MINUS, // -
    MUL, // *
    DIV, // DIV
    LPAREN, // (
    RPAREN, // )
    ID,
    ASSIGN, // :=
    SEMI, // ;
    VAR,
    LOOP,
    DO,
    END,
    EOF,
    // EOF token is used to indicate that
    // there is no more input left for lexical analysis
}

class Token {
    TokenType type;
    String value;

    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}

class Lexer {
    String text;
    int pos;
    char current_char;
    private static final Map<String, Token> RESERVED_KEYWORDS = initMap();

    // map of constant keywords
    private static Map<String, Token> initMap() {
        Map<String, Token> map = new HashMap<>();
        //map.put("PROGRAM", new Token(TokenType.PROGRAM, "PROGRAM"));
        map.put("VAR", new Token(TokenType.VAR, "VAR"));
        map.put("DIV", new Token(TokenType.DIV, "DIV"));
        map.put("INTEGER", new Token(TokenType.INTEGER, "INTEGER"));
        map.put("LOOP", new Token(TokenType.LOOP, "LOOP"));
        map.put("DO", new Token(TokenType.DO, "DO"));
        map.put("END", new Token(TokenType.END, "DO"));
        return Collections.unmodifiableMap(map);
    }

    Lexer(String text) {
        this.text = text;
        this.pos = 0;
        this.current_char = this.text.charAt(this.pos);
    }

    void error() throws Exception {
        throw new Exception("Invalid character!");
    }

    // advance the position and set the current_char variable
    void advance() {
        this.pos++;
        if (this.pos > this.text.length() - 1) {
            this.current_char = '\0'; // End of input
        } else {
            this.current_char = this.text.charAt(this.pos);
        }
    }

    // Return a multi digit integer number token (as a String)
    Token number() {
        StringBuilder result = new StringBuilder();
        while (this.current_char != '\0' && Character.isDigit(this.current_char)) {
            result.append(this.current_char);
            this.advance();
        }
        return new Token(TokenType.INTEGER_CONST, result.toString());
    }

    Token _id() {
        StringBuilder result = new StringBuilder();
        while (this.current_char != '\0' && Character.isLetterOrDigit(this.current_char)) {
            result.append(this.current_char);
            this.advance();
        }
        return RESERVED_KEYWORDS.getOrDefault(result.toString(), new Token(TokenType.ID, result.toString()));
    }

    // Lexical analyzer
    // that breaks a sentence apart into tokens (1 at a time)
    Token get_next_token() throws Exception {
        while (this.current_char != '\0') {
            // skip whitespaces, End of Line, Tab
            if (Character.isSpaceChar(this.current_char) || this.current_char == '\n' || this.current_char == '\t') {
                this.advance();
                continue;
            }

            // skip comments
            if (this.current_char == '{') {
                this.advance(); // starting bracket
                while (this.current_char != '}') {
                    this.advance();
                }
                this.advance(); // closing bracket
                continue;
            }

            if (Character.isLetter(this.current_char)) {
                return this._id();
            }

            if (Character.isDigit(this.current_char)) {
                return this.number();
            }

            if (this.current_char == ':') {
                this.advance();
                this.advance();
                return new Token(TokenType.ASSIGN, ":=");
            }

            if (this.current_char == ';') {
                this.advance();
                return new Token(TokenType.SEMI, ";");
            }


            if (this.current_char == '+') {
                this.advance();
                return new Token(TokenType.PLUS, "+");
            }

            if (this.current_char == '-') {
                this.advance();
                return new Token(TokenType.MINUS, "-");
            }

            if (this.current_char == '*') {
                this.advance();
                return new Token(TokenType.MUL, "*");
            }

            if (this.current_char == '(') {
                this.advance();
                return new Token(TokenType.LPAREN, "(");
            }

            if (this.current_char == ')') {
                this.advance();
                return new Token(TokenType.RPAREN, ")");
            }

            this.error();
        }
        return new Token(TokenType.EOF, "");
    }
}