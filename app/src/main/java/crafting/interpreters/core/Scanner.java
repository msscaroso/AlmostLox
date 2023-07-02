package crafting.interpreters.core;

import crafting.interpreters.core.base.Char;
import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.base.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static crafting.interpreters.core.base.TokenType.*;

public class Scanner {
    private int current = 0;
    private final int last_idx;

    public boolean hadError;
    private final String source;
    private int line = 1;
    private int start = 0;
    private final HashMap<String, TokenType> keywords;
    private final List<Token> tokens = new ArrayList<>();
    public static class ScannerError extends RuntimeException {}
    public Scanner(String source) {
        this.source = source;
        last_idx = source.length() - 1;
        keywords = Token.buildKeywordsMap();
    }

    public List<Token> scanTokens() {

        // already scanned source
        if (tokens.size() > 0) {
            return tokens;
        }

        if (source.length() == 0) {
            return new ArrayList<>();
        }
        while (beforeEnd()) {
            start = current;
            char c = advance();

            switch (c) {
                case ';':
                    addToken(SEMICOLON, ";", null);
                    break;
                case '+':
                    addToken(PLUS, "+", null);
                    break;
                case '(':
                    addToken(LEFT_PAREN, "(", null);
                    break;
                case ')':
                    addToken(RIGHT_PAREN, ")", null);
                    break;
                case '{':
                    addToken(LEFT_BRACE, "{", null);
                    break;
                case '}':
                    addToken(RIGHT_BRACE, "}", null);
                    break;
                case ',':
                    addToken(COMMA, ",", null);
                    break;
                case '.':
                    addToken(DOT, ".", null);
                    break;
                case '-':
                    addToken(MINUS, "-", null);
                    break;
                case '*':
                    addToken(STAR, "*", null);
                    break;
                case '\n':
                    line += 1;
                    break;
                case '/':
                    if (beforeEnd() && peek() == '/') {
                        while (beforeEnd() && peek() != '\n') {
                            advance();
                        }
                        break;
                    }
                    addToken(SLASH, "/", null);
                    break;
                case '=':
                    if (beforeEnd() && peek() == '=') {
                        advance();
                        addToken(EQUAL_EQUAL, "==", null);
                        break;
                    }
                    addToken(EQUAL, "=", null);
                    break;
                case '!':
                    if (beforeEnd() && peek() == '=') {
                        advance();
                        addToken(BANG_EQUAL, "!=", null);
                        break;
                    }
                    addToken(BANG, "!", null);
                    break;
                case '>':
                    if (beforeEnd() && peek() == '=') {
                        advance();
                        addToken(GREATER_EQUAL, ">=", null);
                        break;
                    }
                    addToken(GREATER, ">", null);
                    break;
                case '<':
                    if (beforeEnd() && peek() == '=') {
                        advance();
                        addToken(LESS_EQUAL, "<=", null);
                        break;
                    }
                    addToken(LESS, "<", null);
                    break;
                case '"':
                    string();
                    break;
                case ' ':
                case '\r':
                case '\t':
                    break;
                default:
                    if (Char.isAlpha(c)) {
                        identifier_or_keyword();
                        break;
                    }
                    if (Char.isNumber(c)) {
                        number();
                        break;
                    }
                    error(line, "Unknown char");
            }

        }

        // add EOF if not added yet
        if (tokens.size() == 0 || tokens.get(tokens.size() - 1).type != TokenType.EOF) {
            addToken(EOF, null, null);
        }
        return tokens;
    }

    void addToken(TokenType type, String lexeme, Object literal) {
        tokens.add(new Token(
                type, lexeme, literal, line
        ));
    }

    private char advance() {
        // return 'current' and increment 1
        return source.charAt(current++);
    }

    private char peek() {
        // return 'current' without advancing
        return source.charAt(current);
    }

    private boolean beforeEnd() {
        return current <= last_idx;
    }

    private void string() {
        while (beforeEnd()) {
            if (peek() == '"') {
                advance();
                break;
            }
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (source.charAt(current - 1) != '"') {
            error(line, "Unterminated string");
            return;
        }

        String lex = source.substring(start, current);
        addToken(STRING, lex, source.substring(start + 1, current - 1));
    }

    private void identifier_or_keyword() {
        while (beforeEnd() && Char.isAlphaNumeric(peek())) {
            advance();
        }
        String lex = source.substring(start, current);
        TokenType kw = keywords.get(lex);
        if (kw == null) {
            addToken(IDENTIFIER, lex, null);
            return;
        }
        if (kw == TRUE) {
            addToken(kw, lex, true);
            return;
        }
        if (kw == FALSE) {
            addToken(kw, lex, false);
            return;
        }
        addToken(kw, lex, null);

    }

    private void number() {
        while (beforeEnd() && Char.isAlphaNumeric(peek())) {
            advance();
        }
        if (beforeEnd() && peek() == '.') {
            advance();
            while (beforeEnd() && Char.isAlphaNumeric(peek())) {
                advance();
            }
        }
        String lex = source.substring(start, current);
        addToken(NUMBER, lex, Double.parseDouble(lex));
    }

    void error(int line, String message) {
        System.out.printf("Error in line %d. Error: %s", line, message);
        hadError = true;
    }
}
