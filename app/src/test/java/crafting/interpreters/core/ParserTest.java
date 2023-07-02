package crafting.interpreters.core;

import crafting.interpreters.core.base.Expr;
import crafting.interpreters.core.base.Stmt;
import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.base.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static crafting.interpreters.utils.TestUtil.*;
import static crafting.interpreters.core.base.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    void methodWithTHis() {
//        class Foo {
//              say123() {
//                  print this.x;
//              }
//        }

        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(CLASS),
                getToken(IDENTIFIER, "Foo"),
                getToken(LEFT_BRACE),
                getToken(IDENTIFIER, "say123"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                getToken(THIS),
                getToken(DOT),
                getToken(IDENTIFIER, "x"),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE),
                getToken(RIGHT_BRACE)
        ));

        Stmt.Class expected = new Stmt.Class(
                getToken(IDENTIFIER, "Foo"),
                new ArrayList<>(List.of(
                        new Stmt.Function(
                                getToken(IDENTIFIER, "say123"),
                                new ArrayList<>(),
                                new ArrayList<>(List.of(
                                        new Stmt.Print(
                                                new Expr.Get(
                                                        new Expr.This(getToken(THIS)),
                                                        getToken(IDENTIFIER, "x")
                                                )
                                        )
                                ))
                        )
                ))
        );

        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));

    }

    @Test
    void setProperty() {
        // Foo().x = 1;
        List<Token> tokens = new ArrayList<>(
                Arrays.asList(
                        getToken(IDENTIFIER, "Foo"),
                        getToken(LEFT_PAREN),
                        getToken(RIGHT_PAREN),
                        getToken(DOT),
                        getToken(IDENTIFIER, "x"),
                        getToken(EQUAL),
                        new Token(NUMBER, "1", 1., 1),
                        getToken(SEMICOLON)
                )
        );
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Set(
                        new Expr.Call(
                                new Expr.Variable(getToken(IDENTIFIER, "Foo")),
                                getToken(RIGHT_PAREN),
                                new ArrayList<>()
                        ),
                        getToken(IDENTIFIER, "x"),
                        new Expr.Literal(1.)
                )
        );
        var actual = parseAndAssertSize(tokens, 1);
        compareStmt(expected, actual.get(0));
    }

    @Test
    void readPropertyOfCallReturn() {
        // Foo().x.abc();
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(IDENTIFIER, "Foo"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(DOT),
                getToken(IDENTIFIER, "x"),
                getToken(DOT),
                getToken(IDENTIFIER, "abc"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(SEMICOLON)
        ));
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Call(
                        new Expr.Get(
                                new Expr.Get(
                                        new Expr.Call(
                                                new Expr.Variable(getToken(IDENTIFIER, "Foo")),
                                                getToken(RIGHT_PAREN),
                                                new ArrayList<>()
                                        ),
                                        getToken(IDENTIFIER, "x")
                                ),
                                getToken(IDENTIFIER, "abc")
                        ),
                        getToken(RIGHT_PAREN),
                        new ArrayList<>()
                )
        );
        var actual = parseAndAssertSize(tokens, 1);
        compareStmt(expected, actual.get(0));
    }

    @Test
    void readClassProperty() {
//        x.y;
        List<Token> tokens = new ArrayList<>(
                Arrays.asList(
                        getToken(IDENTIFIER, "x"),
                        getToken(DOT),
                        getToken(IDENTIFIER, "y"),
                        getToken(SEMICOLON)
                )
        );
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Get(
                        new Expr.Variable(getToken(IDENTIFIER, "x")),
                        getToken(IDENTIFIER, "y")
                )
        );
        var actual = parseAndAssertSize(tokens, 1);
        compareStmt(expected, actual.get(0));
    }

    @Test
    void classDeclWithoutMethod() {
//        class Foo {}

        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(CLASS),
                getToken(IDENTIFIER, "Foo"),
                getToken(LEFT_BRACE),
                getToken(RIGHT_BRACE)
        ));

        Stmt.Class expected = new Stmt.Class(
                getToken(IDENTIFIER, "Foo"),
                new ArrayList<>()
        );

        var actual = parseAndAssertSize(tokens, 1);
        compareStmt(expected, actual.get(0));
    }

    @Test
    void callMethod() {
//        class Foo {
//              say123() {
//                  print 123;
//              }
//        }
//        var f = Foo();
//        f.say123();

        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(CLASS),
                getToken(IDENTIFIER, "Foo"),
                getToken(LEFT_BRACE),
                getToken(IDENTIFIER, "say123"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                new Token(NUMBER, "123", 123., 1),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE),
                getToken(RIGHT_BRACE),
                getToken(VAR),
                getToken(IDENTIFIER, "f"),
                getToken(EQUAL),
                getToken(IDENTIFIER, "Foo"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(SEMICOLON),
                getToken(IDENTIFIER, "f"),
                getToken(DOT),
                getToken(IDENTIFIER, "say123"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(SEMICOLON)
        ));
        //  We'll only check the last statement
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Call(
                        new Expr.Get(
                                new Expr.Variable(getToken(IDENTIFIER, "f")),
                                getToken(IDENTIFIER, "say123")
                        ),
                        getToken(RIGHT_PAREN),
                        new ArrayList<>()
                )
        );
        var actual = parseAndAssertSize(tokens, 3);
        compareStmt(expected, actual.get(2));
    }

    @Test
    void classDeclWithMethod() {
//        class Foo {
//              say123() {
//                  print 123;
//              }
//        }

        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(CLASS),
                getToken(IDENTIFIER, "Foo"),
                getToken(LEFT_BRACE),
                getToken(IDENTIFIER, "say123"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                new Token(NUMBER, "123", 123., 1),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE),
                getToken(RIGHT_BRACE)
        ));

        Stmt.Class expected = new Stmt.Class(
                getToken(IDENTIFIER, "Foo"),
                new ArrayList<>(List.of(
                        new Stmt.Function(
                                getToken(IDENTIFIER, "say123"),
                                new ArrayList<>(),
                                new ArrayList<>(List.of(
                                        new Stmt.Print(
                                                new Expr.Literal(123.)
                                        )
                                ))
                        )
                ))
        );

        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));

    }

    @Test
    void funcReturnsValue() {
//        fun a(){return 1;}
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(FUN),
                getToken(IDENTIFIER, "a"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(RETURN),
                new Token(NUMBER, "1", 1., 1),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE)
        ));
        Stmt.Function expected = new Stmt.Function(
                getToken(IDENTIFIER, "a"),
                new ArrayList<>(),
                new ArrayList<>(List.of(
                        new Stmt.Return(getToken(RETURN), new Expr.Literal(1.))
                ))
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void functionDeclWithoutParameters() {
        // fun foo(){}
        List<Token> tokens = new ArrayList<>(
                Arrays.asList(
                        getToken(FUN),
                        getToken(IDENTIFIER, "foo"),
                        getToken(LEFT_PAREN),
                        getToken(RIGHT_PAREN),
                        getToken(LEFT_BRACE),
                        getToken(RIGHT_BRACE)
                )
        );
        Stmt.Function expected = new Stmt.Function(
                getToken(IDENTIFIER, "foo"),
                new ArrayList<>(),
                new ArrayList<>()
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void functionDeclaration() {
        // fun foo(a, b, c){print 123;}
        List<Token> tokens = new ArrayList<>(
                Arrays.asList(
                        getToken(FUN),
                        getToken(IDENTIFIER, "foo"),
                        getToken(LEFT_PAREN),
                        getToken(IDENTIFIER, "a"),
                        getToken(COMMA),
                        getToken(IDENTIFIER, "b"),
                        getToken(COMMA),
                        getToken(IDENTIFIER, "c"),
                        getToken(RIGHT_PAREN),
                        getToken(LEFT_BRACE),
                        getToken(PRINT),
                        new Token(NUMBER, "123", 123., 1),
                        getToken(SEMICOLON),
                        getToken(RIGHT_BRACE)
                )
        );

        Stmt.Function expected = new Stmt.Function(
                getToken(IDENTIFIER, "foo"),
                new ArrayList<>(Arrays.asList(
                        getToken(IDENTIFIER, "a"),
                        getToken(IDENTIFIER, "b"),
                        getToken(IDENTIFIER, "c")
                )),
                new ArrayList<>(List.of(
                        new Stmt.Print(new Expr.Literal(123.))
                ))
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));

    }

    @Test
    void chainedCalls() {
        // foo()();
        List<Token> tokens = new ArrayList<>(
                Arrays.asList(
                        getToken(IDENTIFIER, "foo"),
                        getToken(LEFT_PAREN),
                        getToken(RIGHT_PAREN),
                        getToken(LEFT_PAREN),
                        getToken(RIGHT_PAREN),
                        getToken(SEMICOLON)
                )
        );
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Call(
                        new Expr.Call(
                                new Expr.Variable(getToken(IDENTIFIER, "foo")),
                                getToken(RIGHT_PAREN),
                                new ArrayList<>()
                        ),
                        getToken(RIGHT_PAREN),
                        new ArrayList<>()
                )
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void funcCallWithArgs() {
        // foo(a(x), b, c)();
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(IDENTIFIER, "foo"),
                getToken(LEFT_PAREN),
                getToken(IDENTIFIER, "a"),
                getToken(LEFT_PAREN),
                getToken(IDENTIFIER, "x"),
                getToken(RIGHT_PAREN),
                getToken(COMMA),
                getToken(IDENTIFIER, "b"),
                getToken(COMMA),
                getToken(IDENTIFIER, "c"),
                getToken(RIGHT_PAREN),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(SEMICOLON)
        ));
        Expr.Call a_call = new Expr.Call(
                new Expr.Variable(getToken(IDENTIFIER, "a")),
                getToken(RIGHT_PAREN),
                new ArrayList<>(List.of(
                        new Expr.Variable(getToken(IDENTIFIER, "x"))
                ))
        );
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Call(
                        new Expr.Call(
                                new Expr.Variable(getToken(IDENTIFIER, "foo")),
                                getToken(RIGHT_PAREN),
                                new ArrayList<>(Arrays.asList(
                                        a_call,
                                        new Expr.Variable(getToken(IDENTIFIER, "b")),
                                        new Expr.Variable(getToken(IDENTIFIER, "c"))
                                ))
                        ),
                        getToken(RIGHT_PAREN),
                        new ArrayList<>()
                )
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void funcCall() {
        // x = foo();
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(IDENTIFIER, "x"),
                getToken(EQUAL),
                getToken(IDENTIFIER, "foo"),
                getToken(LEFT_PAREN),
                getToken(RIGHT_PAREN),
                getToken(SEMICOLON)
        ));
        Stmt.ExprStmt expected = new Stmt.ExprStmt(
                new Expr.Assignment(
                        getToken(IDENTIFIER, "x"),
                        new Expr.Call(
                                new Expr.Variable(getToken(IDENTIFIER, "foo")),
                                getToken(RIGHT_PAREN),
                                new ArrayList<>()
                        )
                )
        );
        var parser = new Parser(tokens);
        List<Stmt> actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void forTest() {
//        for (var i = 0; i < 10; i = i + 1){
//              print i
//        }
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(FOR),
                getToken(LEFT_PAREN),
                getToken(VAR),
                getToken(IDENTIFIER, "i"),
                getToken(EQUAL),
                new Token(NUMBER, "0", 0., 1),
                getToken(SEMICOLON),
                getToken(IDENTIFIER, "i"),
                getToken(LESS),
                new Token(NUMBER, "10", 10., 1),
                getToken(SEMICOLON),
                getToken(IDENTIFIER, "i"),
                getToken(EQUAL),
                getToken(IDENTIFIER, "i"),
                getToken(PLUS),
                new Token(NUMBER, "1", 1., 1),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                getToken(IDENTIFIER, "i"),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE)
        ));
        Stmt expected1 = new Stmt.VarDcl(
                getToken(IDENTIFIER, "i"),
                new Expr.Literal(0.)
        );
        var condition = new Expr.Binary(
                new Expr.Variable(getToken(IDENTIFIER, "i")),
                new Expr.Literal(10.),
                getToken(LESS)
        );
        var increment = new Stmt.ExprStmt(
                new Expr.Assignment(
                        getToken(IDENTIFIER, "i"),
                        new Expr.Binary(
                                new Expr.Variable(getToken(IDENTIFIER, "i")),
                                new Expr.Literal(1.),
                                getToken(PLUS)
                        )
                )
        );
        var body = new Stmt.Block(
                new ArrayList<>(List.of(
                        new Stmt.Print(
                                new Expr.Variable(getToken(IDENTIFIER, "i"))
                        )
                )
                )
        );
        var bodyWithInc = new Stmt.Block(
                new ArrayList<>(Arrays.asList(
                        body,
                        increment

                ))
        );
        Stmt expected2 = new Stmt.While(condition, bodyWithInc);
        Stmt.Block expected = new Stmt.Block(
                new ArrayList<>(Arrays.asList(
                        expected1,
                        expected2
                ))
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();

        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void whileTest() {
//        while (x < 10) {x = x + 1}
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(WHILE),
                getToken(LEFT_PAREN),
                getToken(IDENTIFIER, "x"),
                getToken(LESS),
                new Token(NUMBER, "10", 10., 1),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(IDENTIFIER, "x"),
                getToken(EQUAL),
                getToken(IDENTIFIER, "x"),
                getToken(PLUS),
                new Token(NUMBER, "1", 1., 1),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE)
        ));
        Stmt expected = new Stmt.While(
                new Expr.Binary(
                        new Expr.Variable(getToken(IDENTIFIER, "x")),
                        new Expr.Literal(10.),
                        getToken(LESS)
                ),
                new Stmt.Block(
                        new ArrayList<>(List.of(
                                new Stmt.ExprStmt(
                                        new Expr.Assignment(
                                                getToken(IDENTIFIER, "x"),
                                                new Expr.Binary(
                                                        new Expr.Variable(getToken(IDENTIFIER, "x")),
                                                        new Expr.Literal(1.),
                                                        getToken(PLUS)
                                                )
                                        )
                                )
                        ))
                )
        );

        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void logicOrAnd() {
        // y = 1 or x and 2;
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(IDENTIFIER, "y"),
                getToken(EQUAL),
                new Token(NUMBER, "1", 1., 1),
                getToken(OR),
                getToken(IDENTIFIER, "x"),
                getToken(AND),
                new Token(NUMBER, "2", 2., 1),
                getToken(SEMICOLON)
        ));
        Stmt expected = new Stmt.ExprStmt(
                new Expr.Assignment(
                        getToken(IDENTIFIER, "y"),
                        new Expr.Logical(
                                new Expr.Literal(1.),
                                getToken(OR),
                                new Expr.Logical(
                                        new Expr.Variable(getToken(IDENTIFIER, "x")),
                                        getToken(AND),
                                        new Expr.Literal(2.)
                                )
                        )
                )
        );

        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void IfElse() {
//        if (x == 1) {
//           print x;
//        }
//        else {
//              print x + 1;
//        }

        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(IF),
                getToken(LEFT_PAREN),
                getToken(IDENTIFIER, "x"),
                getToken(EQUAL_EQUAL),
                new Token(NUMBER, "1", 1., 1),
                getToken(RIGHT_PAREN),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                getToken(IDENTIFIER, "x"),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE),
                getToken(ELSE),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                getToken(IDENTIFIER, "x"),
                getToken(PLUS),
                new Token(NUMBER, "1", 1., 1),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE)
        ));
        Expr.Binary condition = new Expr.Binary(
                new Expr.Variable(getToken(IDENTIFIER, "x")),
                new Expr.Literal(1.),
                getToken(EQUAL_EQUAL)
        );
        Stmt.Block thenBranch = new Stmt.Block(
                new ArrayList<>(List.of(
                        new Stmt.Print(new Expr.Variable(getToken(IDENTIFIER, "x")))
                ))
        );
        Stmt.Block elseBranch = new Stmt.Block(
                new ArrayList<>(List.of(
                        new Stmt.Print(new Expr.Binary(
                                new Expr.Variable(getToken(IDENTIFIER, "x")),
                                new Expr.Literal(1.),
                                getToken(PLUS)
                        ))
                ))
        );

        Stmt expected = new Stmt.If(
                condition,
                thenBranch,
                elseBranch
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void block() {
        // var x = 1;
        // { print x; }
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(VAR),
                getToken(IDENTIFIER, "x"),
                getToken(EQUAL),
                new Token(NUMBER, "1", 1., 1),
                getToken(SEMICOLON),
                getToken(LEFT_BRACE),
                getToken(PRINT),
                getToken(IDENTIFIER, "x"),
                getToken(SEMICOLON),
                getToken(RIGHT_BRACE)
        ));
        Stmt expected1 = new Stmt.VarDcl(
                getToken(IDENTIFIER, "x"),
                new Expr.Literal(1.)
        );
        Stmt expected2 = new Stmt.Block(
                new ArrayList<>(List.of(
                        new Stmt.Print(
                                new Expr.Variable(getToken(IDENTIFIER, "x"))
                        )
                ))
        );
        var stmts = parseAndAssertSize(tokens, 2);
        compareStmt(expected1, stmts.get(0));
        compareStmt(expected2, stmts.get(1));
    }

    @Test
    void assignment() {
        //  var x = 1;
        //  x = 2 + 3;
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "1", 1., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.EQUAL, "=", null, 2),
                new Token(TokenType.NUMBER, "2", 2., 2),
                new Token(PLUS, "+", null, 2),
                new Token(TokenType.NUMBER, "3", 3., 2),
                new Token(TokenType.SEMICOLON, ";", null, 2)
        ));

        Stmt expected1 = new Stmt.VarDcl(
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Expr.Literal(1.)
        );

        Stmt expected2 = new Stmt.ExprStmt(new Expr.Assignment(
                new Token(IDENTIFIER, "x", null, 2),
                new Expr.Binary(
                        new Expr.Literal(2.),
                        new Expr.Literal(3.),
                        new Token(PLUS, "+", null, 2)
                )
        ));

        var parser = new Parser(tokens);
        var stmts = parser.parse();
        assertEquals(stmts.size(), 2);
        compareStmt(expected1, stmts.get(0));
        compareStmt(expected2, stmts.get(1));
    }

    @Test
    void variableDeclaration() {
//        var x = 1;
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(VAR, "var", null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.NUMBER, "1", 1., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.VarDcl(
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Expr.Literal(1.)
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void variableDeclarationAndUsage() {
//        var x = 1;
//        print x * (-5 + 1);
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                getToken(VAR, "var"),
                getToken(IDENTIFIER, "x"),
                getToken(EQUAL, "="),
                new Token(NUMBER, "1", 1., 1),
                getToken(SEMICOLON, ";"),
                getToken(PRINT, "print"),
                getToken(IDENTIFIER, "x"),
                getStar(),
                getToken(LEFT_PAREN, "("),
                getToken(MINUS, "-"),
                new Token(NUMBER, "5", 5., 1),
                getToken(PLUS, "+"),
                new Token(NUMBER, "1", 1., 1),
                getToken(RIGHT_PAREN, ")"),
                getSemiColon()
        ));
//        print x * -5 + 1
        Stmt expected1 = new Stmt.VarDcl(
                getToken(IDENTIFIER, "x"),
                new Expr.Literal(1.)
        );
        Stmt expected2 = new Stmt.Print(
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
        );

        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(2, actual.size());
        compareStmt(expected1, actual.get(0));
        compareStmt(expected2, actual.get(1));

    }

    @Test
    void singleStatement() {
        // 2 == 3;
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.NUMBER, "2", 2., 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, "3", 3., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.ExprStmt(
                new Expr.Binary(
                        new Expr.Literal(2.),
                        new Expr.Literal(3.),
                        getEqualEqual()
                )
        );
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void printSum() {
//        print 1 + 1
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(PRINT, "print", null, 1),
                new Token(TokenType.NUMBER, "1", 1., 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "1", 1., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.Print(
                new Expr.Binary(
                        new Expr.Literal(1.),
                        new Expr.Literal(1.),
                        getPlus()
                )
        );
        Parser parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));

    }

    @Test
    void compareNil() {
        // nil == 1
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.NIL, "nil", null, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, "1", 1., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.ExprStmt(new Expr.Binary(
                new Expr.Literal(null),
                new Expr.Literal(1.0),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1)
        ));

        Parser parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void sumAndMultiply() {
        // 1 + 5 * 10
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "5", 5., 1),
                new Token(TokenType.STAR, "*", null, 1),
                new Token(TokenType.NUMBER, "10", 10., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.ExprStmt(new Expr.Binary(
                new Expr.Literal(1.0),
                new Expr.Binary(
                        new Expr.Literal(5.0),
                        new Expr.Literal(10.0),
                        new Token(TokenType.STAR, "*", null, 1)
                ),
                new Token(TokenType.PLUS, "+", null, 1)

        ));


        Parser parser = new Parser(tokens);


        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void groupedSumAndMultiply() {
        // (1 + 5) * 10
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.LEFT_PAREN, "(", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "5", 5., 1),
                new Token(TokenType.RIGHT_PAREN, ")", null, 1),
                new Token(TokenType.STAR, "*", null, 1),
                new Token(TokenType.NUMBER, "10", 10., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1),
                new Token(TokenType.EOF, null, null, 1)
        ));

        Stmt expected = new Stmt.ExprStmt(new Expr.Binary(
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(1.0),
                                new Expr.Literal(5.0),
                                new Token(TokenType.PLUS, "+", null, 1)
                        )
                ),
                new Expr.Literal(10.0),
                new Token(TokenType.STAR, "*", null, 1)
        ));


        Parser parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));
    }

    @Test
    void unbalancedParenLeft() {
        // (1 + 5 * 10
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.LEFT_PAREN, "(", null, 1),
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "5", 5., 1),
                new Token(TokenType.STAR, "*", null, 1),
                new Token(TokenType.NUMBER, "10", 10., 1)
        ));
        Parser parser = new Parser(tokens);
        try {
            parser.parse();
            fail("expected exception");
        } catch (Parser.ParserError ignored) {
        }
    }

    @Test
    void unbalancedParenRight() {
        // 1 + 5 * 10)
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "5", 5., 1),
                new Token(TokenType.STAR, "*", null, 1),
                new Token(TokenType.NUMBER, "10", 10., 1),
                new Token(TokenType.RIGHT_PAREN, ")", null, 1)
        ));
        Parser parser = new Parser(tokens);
        try {
            parser.parse();
            fail("expected exception");
        } catch (Parser.ParserError ignored) {
        }
    }

    @ParameterizedTest
    @MethodSource("paramsComparison")
    void testComparison(TokenType tt, String lex) {
        // 1 > 5 + 2 * 10
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(tt, lex, null, 1),
                new Token(TokenType.NUMBER, "5", 5., 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "2", 2., 1),
                new Token(TokenType.STAR, "*", null, 1),
                new Token(TokenType.NUMBER, "10", 10., 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.ExprStmt(new Expr.Binary(
                new Expr.Literal(1.0),
                new Expr.Binary(
                        new Expr.Literal(5.0),
                        new Expr.Binary(
                                new Expr.Literal(2.0),
                                new Expr.Literal(10.0),
                                new Token(TokenType.STAR, "*", null, 1)
                        ),
                        new Token(TokenType.PLUS, "+", null, 1)
                ),
                new Token(tt, lex, null, 1)
        ));

        Parser parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));

    }

    private static Stream<Arguments> paramsComparison() {
        return Stream.of(
                Arguments.of(TokenType.GREATER, ">"),
                Arguments.of(TokenType.GREATER_EQUAL, ">="),
                Arguments.of(TokenType.LESS, "<"),
                Arguments.of(TokenType.LESS_EQUAL, "<=")
        );
    }

    @ParameterizedTest
    @MethodSource("paramsEquality")
    void testEquality(TokenType tt, String lex) {
        // 1 == 2
        List<Token> tokens = new ArrayList<>(Arrays.asList(
                new Token(TokenType.NUMBER, "1", 1.0, 1),
                new Token(tt, lex, null, 1),
                new Token(TokenType.NUMBER, "2", 2.0, 1),
                new Token(TokenType.SEMICOLON, ";", null, 1)
        ));
        Stmt expected = new Stmt.ExprStmt(new Expr.Binary(new Expr.Literal(1.0), new Expr.Literal(2.0), new Token(tt, lex, null, 1)));
        Parser parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(1, actual.size());
        compareStmt(expected, actual.get(0));

    }

    private static Stream<Arguments> paramsEquality() {
        return Stream.of(
                Arguments.of(TokenType.BANG_EQUAL, "!="),
                Arguments.of(TokenType.EQUAL_EQUAL, "==")
        );
    }

    List<Stmt> parseAndAssertSize(List<Token> tokens, int expectedSize) {
        var parser = new Parser(tokens);
        var actual = parser.parse();
        assertEquals(expectedSize, actual.size());
        return actual;
    }
}
