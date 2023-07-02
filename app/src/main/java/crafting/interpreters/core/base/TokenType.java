package crafting.interpreters.core.base;

public enum TokenType {
    //    Tokens with single characters
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS,
    SEMICOLON, SLASH, STAR,

    // Tokens with varying number of characters
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, IDENTIFIER, STRING, NUMBER,

    // Keywords

    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF

}


