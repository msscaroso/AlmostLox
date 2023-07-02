package crafting.interpreters.core.base;

import java.util.HashMap;
import java.util.Objects;

public class Token {
    final public TokenType type;
    final public String lexeme;

    final public int line;

    final public Object literal;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token t) {
            return type == t.type && Objects.equals(lexeme, t.lexeme) && Objects.equals(literal, t.literal) && line == t.line;
        }
        return super.equals(obj);
    }

    public static HashMap<String, TokenType> buildKeywordsMap() {
        HashMap<String, TokenType> keywords = new HashMap<>();
        keywords.put("var", TokenType.VAR);
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("while", TokenType.WHILE);
        return keywords;
    }
}
