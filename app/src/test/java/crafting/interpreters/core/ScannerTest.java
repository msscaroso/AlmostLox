package crafting.interpreters.core;

import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.base.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScannerTest {

    @Test
    void simpleAssignment() {
        String source = "var N = 1.5;";

        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "N", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "1.5", 1.5, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));

        Scanner s = new Scanner(source);
        List<Token> tokens = s.scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }

    @Test
    void multipleLines() {
        String source = "var N = 1; var x =2;\nvar y = N + x;";

        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "N", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "2", 2.0, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.VAR, "var", null, 2),
                new Token(TokenType.IDENTIFIER, "y", null, 2),
                new Token(TokenType.EQUAL, "=", null, 2),
                new Token(TokenType.IDENTIFIER, "N", null, 2),
                new Token(TokenType.PLUS, "+", null, 2),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.SEMICOLON, ";", null, 2),
                new Token(TokenType.EOF, null, null, 2)
        ));

        Scanner s = new Scanner(source);
        List<Token> tokens = s.scanTokens();
        compareListOfToken(expectedTokens, tokens);
        // check method idempotent
        compareListOfToken(expectedTokens, s.scanTokens());
    }

    @Test
    void equalEqual(){
        String source = "x == 1;";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();

        compareListOfToken(expectedTokens, tokens);

    }
    @Test
    void bang(){
        String source = "!x != true";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.BANG, "!", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.BANG_EQUAL, "!=", null, 1),
                new Token(TokenType.TRUE, "true", true, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));
        List <Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    @Test
    void greaterEqual(){
        String source = "x >= 1 and y > 1;";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.GREATER_EQUAL, ">=", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.AND, "and", null, 1),
                new Token(TokenType.IDENTIFIER, "y", null, 1),
                new Token(TokenType.GREATER, ">", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }

    @Test
    void lessEqual(){
        String source = "x <= 1 and y < 1;";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.LESS_EQUAL, "<=", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.AND, "and", null, 1),
                new Token(TokenType.IDENTIFIER, "y", null, 1),
                new Token(TokenType.LESS, "<", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    @Test
    void slash(){
        String source = "// a comment \n x / 1;";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.SLASH, "/", null, 2),
                new Token(TokenType.NUMBER, "1", 1.0, 2),
                new Token(TokenType.SEMICOLON, ";", null, 2),
                new Token(TokenType.EOF, null, null, 2)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    @Test
    void comment () {
        String source = "// this is a comment";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.EOF, null, null, 1)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    @Test
    void string(){
        String source = "var x = \"hello world\";";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.STRING, "\"hello world\"", "hello world", 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    @Test
    void stringMultipleLines(){
        String source = "var x = \"hello\nworld\";";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.STRING, "\"hello\nworld\"", "hello\nworld", 2),
                new Token(TokenType.SEMICOLON, ";", null, 2),
                new Token(TokenType.EOF, null, null, 2)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    @Test
    void unterminatedString(){
        String source = "var x = \"hello world;";
        List<Token> expectedTokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));
        List<Token> tokens = new Scanner(source).scanTokens();
        compareListOfToken(expectedTokens, tokens);
    }
    void compareListOfToken(List<Token> expected, List<Token> actual) {
        String error_msg = "Expected and Actual has different %s. Index: %d";
        assertEquals(expected.size(), actual.size(), "Actual list has a different size than expected");
        for (int i = 0; i < actual.size(); i++) {
            Token e = expected.get(i);
            Token a = actual.get(i);
            assertEquals(e.type, a.type, String.format(error_msg, "types", i));
            assertEquals(e.lexeme, a.lexeme, String.format(error_msg, "lexemes", i));
            assertEquals(e.literal, a.literal, String.format(error_msg, "literals", i));
            assertEquals(e.line, a.line, String.format(error_msg, "lines", i));
        }
    }

}

