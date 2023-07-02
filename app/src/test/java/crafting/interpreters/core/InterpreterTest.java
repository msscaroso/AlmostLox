package crafting.interpreters.core;

import crafting.interpreters.core.base.Expr;
import crafting.interpreters.core.base.Stmt;
import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.base.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static crafting.interpreters.utils.TestUtil.*;
import static crafting.interpreters.core.base.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class InterpreterTest {

    static class Cp extends Interpreter.CapturePrint {

    }

    @Test
    void sumVars() {
        // var x = 5;
        // print x + x;
        List<Stmt> stmts = List.of(
                new Stmt.VarDcl(
                        getToken(IDENTIFIER, "x"),
                        new Expr.Literal(5.)
                ),
                new Stmt.Print(
                        new Expr.Binary(
                                new Expr.Variable(getToken(IDENTIFIER, "x")),
                                new Expr.Variable(getToken(IDENTIFIER, "x")),
                                getPlus()
                        )
                )
        );
        var cp = new Cp();
        var interpreter = new Interpreter(stmts, cp);
        interpreter.interpret();
        Object actual = cp.capturedValue;
        assertEquals(10., (Double) actual);
    }

    @Test
    void varDeclAndVar() {
        //        var x = 1;
//        print x * (-5 + 1);
        List<Stmt> stmts = List.of(new Stmt.VarDcl(
                        getToken(IDENTIFIER, "x"),
                        new Expr.Literal(2.)
                ),
                new Stmt.Print(
                        new Expr.Binary(
                                new Expr.Variable(getToken(IDENTIFIER, "x")),
                                new Expr.Grouping(
                                        new Expr.Binary(
                                                new Expr.Unary(
                                                        getToken(MINUS, "-"),
                                                        new Expr.Literal(5.)
                                                ),
                                                new Expr.Literal(1.),
                                                getToken(PLUS, "+")
                                        )
                                ),
                                new Token(STAR, "*", null, 1)
                        )
                )

        );
        var cp = new Cp();
        var interpreter = new Interpreter(stmts, cp);
        interpreter.interpret();
        Object actual = cp.capturedValue;
        assertEquals(-8., (Double) actual);
    }

    @Test
    void grouping() {
        // 10 / (-10 + 12)
        Stmt stmt = new Stmt.Print(new Expr.Binary(
                new Expr.Literal(10.),
                new Expr.Grouping(new Expr.Binary(
                        new Expr.Unary(
                                getMinus(),
                                new Expr.Literal(10.)
                        ),
                        new Expr.Literal(12.),
                        getPlus()
                )),
                getSlash()
        ));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(5., (Double) actual);
    }

    @ParameterizedTest
    @MethodSource("paramsUnaryBang")
    void unaryBang(Object val, Boolean expected) {
        // !val == true
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Unary(new Token(TokenType.BANG, "1", null, 1), new Expr.Literal(val)), new Expr.Literal(true), new Token(TokenType.EQUAL_EQUAL, "==", null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(expected, (Boolean) actual);
    }

    static private Stream<Arguments> paramsUnaryBang() {
        return Stream.of(
                Arguments.of(1, false),
                Arguments.of(0, false),
                Arguments.of(-5, false),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(null, true)
        );
    }

    @Test
    void unaryMinus() {
        // -10/2
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(10.)), new Expr.Literal(2.), new Token(TokenType.SLASH, "/", null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(-5., (Double) actual);
    }

    @ParameterizedTest
    @MethodSource("paramsMultiAndDiv")
    void multiAndDiv(TokenType tt, String lex, Double expected) {
        // 1 / 2
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(1.), new Expr.Literal(2.), new Token(tt, lex, null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(expected, (Double) actual);
    }

    private static Stream<Arguments> paramsMultiAndDiv() {
        return Stream.of(Arguments.of(TokenType.SLASH, "/", 0.5), Arguments.of(TokenType.STAR, "*", 2.));
    }

    @Test
    void plusAndMinus() {
        // 1 + 2 - 5
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(1.0), new Expr.Binary(new Expr.Literal(2.0), new Expr.Literal(5.), new Token(TokenType.MINUS, "-", null, 1)), new Token(TokenType.PLUS, "+", null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();

        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(-2., (Double) actual);
    }

    @Test
    void compareExpressions() {
        // 10 + 5 == 115 - 100
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Binary(new Expr.Literal(10.), new Expr.Literal(5.), new Token(TokenType.PLUS, "+", null, 1)), new Expr.Binary(new Expr.Literal(115.0), new Expr.Literal(100.), new Token(TokenType.MINUS, "-", null, 1)), new Token(TokenType.EQUAL_EQUAL, "==", null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();

        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(true, (Boolean) actual);

    }

    @ParameterizedTest
    @MethodSource("paramsOpNumberAndNil")
    void opNumberAndNil(TokenType tt, String lex) {
        // 1 + nil
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(1.), new Expr.Literal(null), new Token(tt, lex, null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        try {
            interpreter.interpret();
            fail("expected to fail while trying op nil and number");
        } catch (RuntimeException ignored) {

        }
    }

    static private Stream<Arguments> paramsOpNumberAndNil() {
        return Stream.of(Arguments.of(TokenType.PLUS, "+"), Arguments.of(TokenType.MINUS, "-"), Arguments.of(TokenType.STAR, "*"), Arguments.of(TokenType.SLASH, "/"));
    }

    @Test
    void compareNilToNumber() {
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(null), new Expr.Literal(1.0), new Token(TokenType.EQUAL_EQUAL, "==", null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();

        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(false, (Boolean) actual);

    }

    @Test
    void compareNilToNil() {
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(null), new Expr.Literal(null), new Token(TokenType.EQUAL_EQUAL, "==", null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(true, (Boolean) actual);
    }

    @ParameterizedTest
    @MethodSource("paramsEquality")
    void equality(TokenType tt, String lex, Boolean expected) {
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(1.0), new Expr.Literal(2.0), new Token(tt, lex, null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(expected, (Boolean) actual);
    }

    @ParameterizedTest
    @MethodSource("paramsTerm")
    void term(TokenType tt, String lex, Double expected) {
        Stmt stmt = new Stmt.Print(new Expr.Binary(new Expr.Literal(1.0), new Expr.Literal(2.0), new Token(tt, lex, null, 1)));
        var cp = new Cp();
        Interpreter interpreter = new Interpreter(List.of(stmt), cp);
        interpreter.interpret();
        Object actual = null;
        if (cp.captured) {
            actual = cp.capturedValue;
        }
        assertEquals(expected, (Double) actual);
    }

    private static Stream<Arguments> paramsTerm() {
        return Stream.of(Arguments.of(TokenType.PLUS, "+", 3.), Arguments.of(TokenType.MINUS, "-", -1.));
    }

    private static Stream<Arguments> paramsEquality() {
        return Stream.of(Arguments.of(TokenType.BANG_EQUAL, "!=", true), Arguments.of(TokenType.EQUAL_EQUAL, "==", false));
    }
}
