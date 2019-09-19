package InterpreterAlpha;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

enum TokenType {
    INTEGER,
    REAL,
    INTEGER_CONST,
    REAL_CONST,
    PLUS, // +
    MINUS, // -
    MUL, // *
    INTEGER_DIV, // DIV
    FLOAT_DIV, // /
    LPAREN, // (
    RPAREN, // )
    ID,
    ASSIGN, // :=
    BEGIN,
    END,
    SEMI, // ;
    DOT, // .
    PROGRAM,
    VAR,
    COLON, // :
    COMMA, // ,
    LOOP,
    DO,
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
        map.put("PROGRAM", new Token(TokenType.PROGRAM, "PROGRAM"));
        map.put("VAR", new Token(TokenType.VAR, "VAR"));
        map.put("DIV", new Token(TokenType.INTEGER_DIV, "DIV"));
        map.put("INTEGER", new Token(TokenType.INTEGER, "INTEGER"));
        map.put("REAL", new Token(TokenType.REAL, "REAL"));
        map.put("BEGIN", new Token(TokenType.BEGIN, "BEGIN"));
        map.put("END", new Token(TokenType.END, "END"));
        map.put("LOOP", new Token(TokenType.LOOP, "LOOP"));
        map.put("DO", new Token(TokenType.DO, "DO"));
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

    // look at the next character
    char peek() {
        int peek_pos = this.pos + 1;
        if (peek_pos > this.text.length() - 1) {
            return '\0';
        } else {
            return this.text.charAt(peek_pos);
        }
    }

    // Return a multi digit integer or real number token (as a String)
    Token number() {
        StringBuilder result = new StringBuilder();
        while (this.current_char != '\0' && Character.isDigit(this.current_char)) {
            result.append(this.current_char);
            this.advance();
        }

        if (this.current_char == '.') {
            result.append(this.current_char);
            this.advance();

            while (this.current_char != '\0' && Character.isDigit(this.current_char)) {
                result.append(this.current_char);
                this.advance();
            }
            return new Token(TokenType.REAL_CONST, result.toString());
        } else
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

            if (this.current_char == ':' && this.peek() == '=') {
                this.advance();
                this.advance();
                return new Token(TokenType.ASSIGN, ":=");
            }

            if (this.current_char == ';') {
                this.advance();
                return new Token(TokenType.SEMI, ";");
            }

            if (this.current_char == ':') {
                this.advance();
                return new Token(TokenType.COLON, ":");
            }

            if (this.current_char == ',') {
                this.advance();
                return new Token(TokenType.COMMA, ",");
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

            if (this.current_char == '/') {
                this.advance();
                return new Token(TokenType.FLOAT_DIV, "/");
            }

            if (this.current_char == '(') {
                this.advance();
                return new Token(TokenType.LPAREN, "(");
            }

            if (this.current_char == ')') {
                this.advance();
                return new Token(TokenType.RPAREN, ")");
            }

            if (this.current_char == '.') {
                this.advance();
                return new Token(TokenType.DOT, ".");
            }

            this.error();
        }
        return new Token(TokenType.EOF, "");
    }
}