package crafting.interpreters.utils;

import crafting.interpreters.core.base.Expr;
import crafting.interpreters.core.base.Stmt;
import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.base.TokenType;

import java.util.HashMap;

import static crafting.interpreters.core.base.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtil {

    public static Token getToken(TokenType tt) {
        HashMap<TokenType, String> tokenWithConstLex = new HashMap<>();
        tokenWithConstLex.put(VAR, "var");
        tokenWithConstLex.put(EQUAL, "=");
        tokenWithConstLex.put(EQUAL_EQUAL, "==");
        tokenWithConstLex.put(BANG_EQUAL, "!=");
        tokenWithConstLex.put(PLUS, "+");
        tokenWithConstLex.put(MINUS, "-");
        tokenWithConstLex.put(STAR, "*");
        tokenWithConstLex.put(SLASH, "/");
        tokenWithConstLex.put(LEFT_PAREN, "(");
        tokenWithConstLex.put(RIGHT_PAREN, ")");
        tokenWithConstLex.put(LEFT_BRACE, "{");
        tokenWithConstLex.put(RIGHT_BRACE, "}");
        tokenWithConstLex.put(SEMICOLON, ";");
        tokenWithConstLex.put(COMMA, ",");
        tokenWithConstLex.put(DOT, ".");
        tokenWithConstLex.put(BANG, "!");
        tokenWithConstLex.put(LESS, "<");
        tokenWithConstLex.put(LESS_EQUAL, "<=");
        tokenWithConstLex.put(GREATER, ">");
        tokenWithConstLex.put(GREATER_EQUAL, ">=");
        tokenWithConstLex.put(AND, "and");
        tokenWithConstLex.put(CLASS, "class");
        tokenWithConstLex.put(ELSE, "else");
        tokenWithConstLex.put(FALSE, "false");
        tokenWithConstLex.put(FOR, "for");
        tokenWithConstLex.put(IF, "if");
        tokenWithConstLex.put(NIL, "nil");
        tokenWithConstLex.put(OR, "or");
        tokenWithConstLex.put(PRINT, "print");
        tokenWithConstLex.put(RETURN, "return");
        tokenWithConstLex.put(SUPER, "super");
        tokenWithConstLex.put(THIS, "this");
        tokenWithConstLex.put(TRUE, "true");
        tokenWithConstLex.put(WHILE, "while");
        tokenWithConstLex.put(FUN, "fun");
        return getToken(tt, tokenWithConstLex.get(tt));
    }

    public static Token getToken(TokenType tt, String lex) {
        return new Token(tt, lex, null, 1);
    }

    public static Token getMinus() {
        return getToken(MINUS, "-");
    }

    public static Token getPlus() {
        return getToken(PLUS, "+");
    }

    public static Token getSlash() {
        return getToken(SLASH, "/");
    }

    public static Token getStar() {
        return getToken(STAR, "*");
    }

    public static Token getEqualEqual() {
        return getToken(EQUAL_EQUAL, "==");
    }

    public static Token getSemiColon() {
        return getToken(SEMICOLON, ";");
    }

    public static void compareStmt(Stmt expected, Stmt actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (actual == null) {
            fail("actual is null, but it was not expected");
        }

        assert expected != null;
        assertEquals(expected.getClass(), actual.getClass(), "two different stmt");
        if (expected instanceof Stmt.Print) {
            compareExpr(((Stmt.Print) expected).expression, ((Stmt.Print) actual).expression);
            return;
        }
        if (expected instanceof Stmt.ExprStmt) {
            compareExpr(((Stmt.ExprStmt) expected).expression, ((Stmt.ExprStmt) actual).expression);
            return;
        }
        if (expected instanceof Stmt.VarDcl) {
            compareStmt((Stmt.VarDcl) expected, (Stmt.VarDcl) actual);
            return;
        }
        if (expected instanceof Stmt.Block) {
            compareStmt((Stmt.Block) expected, (Stmt.Block) actual);
            return;
        }
        if (expected instanceof Stmt.If) {
            compareStmt((Stmt.If) expected, (Stmt.If) actual);
            return;
        }
        if (expected instanceof Stmt.While) {
            compareStmt((Stmt.While) expected, (Stmt.While) actual);
            return;
        }
        if (expected instanceof Stmt.Function) {
            compareStmt((Stmt.Function) expected, (Stmt.Function) actual);
            return;
        }
        if (expected instanceof Stmt.Return) {
            compareStmt((Stmt.Return) expected, (Stmt.Return) actual);
            return;
        }
        if (expected instanceof Stmt.Class) {
            compareStmt((Stmt.Class) expected, (Stmt.Class) actual);
            return;
        }
        fail("unexpected stmt");
    }

    public static void compareStmt(Stmt.Class expected, Stmt.Class actual) {
        compareToken(expected.name, actual.name);
        assertEquals(expected.methods.size(), actual.methods.size());
        for (var i = 0; i < expected.methods.size(); i++) {
            compareStmt(expected.methods.get(i), actual.methods.get(i));
        }
    }

    public static void compareStmt(Stmt.Function expected, Stmt.Function actual) {
        compareToken(expected.name, actual.name);
        assertEquals(expected.params.size(), actual.params.size());
        for (var i = 0; i < expected.params.size(); i++) {
            compareToken(expected.params.get(i), actual.params.get(i));
        }
        assertEquals(expected.body.size(), actual.body.size());
        for (var i = 0; i < expected.body.size(); i++) {
            compareStmt(expected.body.get(i), actual.body.get(i));
        }
    }

    public static void compareStmt(Stmt.While expected, Stmt.While actual) {
        compareExpr(expected.condition, actual.condition);
        compareStmt(expected.statement, actual.statement);
    }

    public static void compareStmt(Stmt.If expected, Stmt.If actual) {
        compareExpr(expected.condition, actual.condition);
        compareStmt(expected.thenBranch, actual.thenBranch);
        compareStmt(expected.elseBranch, actual.elseBranch);
    }

    public static void compareStmt(Stmt.Block expected, Stmt.Block actual) {
        assertEquals(expected.statements.size(), actual.statements.size());
        for (int i = 0; i < expected.statements.size(); i++) {
            compareStmt(expected.statements.get(i), actual.statements.get(i));
        }
    }

    public static void compareStmt(Stmt.VarDcl expected, Stmt.VarDcl actual) {
        assertEquals(expected.name.lexeme, actual.name.lexeme);
        assertEquals(expected.name.type, actual.name.type);
        assertEquals(expected.name.literal, actual.name.literal);
        assertEquals(expected.name.line, actual.name.line);

        compareExpr(expected.expressionInitializer, actual.expressionInitializer);

    }

    public static void compareExpr(Expr expected, Expr actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (actual == null) {
            fail("actual is null, but it was not expected");
        }

        assert expected != null;
        assertEquals(expected.getClass(), actual.getClass(), "two different exp");
        if (expected instanceof Expr.Binary) {
            compareExpr((Expr.Binary) expected, (Expr.Binary) actual);
            return;
        }
        if (expected instanceof Expr.Literal) {
            compareExpr((Expr.Literal) expected, (Expr.Literal) actual);
            return;
        }
        if (expected instanceof Expr.Grouping) {
            compareExpr((Expr.Grouping) expected, (Expr.Grouping) actual);
            return;
        }
        if (expected instanceof Expr.Unary) {
            compareExpr((Expr.Unary) expected, (Expr.Unary) actual);
            return;
        }
        if (expected instanceof Expr.Variable) {
            compareExpr((Expr.Variable) expected, (Expr.Variable) actual);
            return;
        }

        if (expected instanceof Expr.Assignment) {
            compareExpr((Expr.Assignment) expected, (Expr.Assignment) actual);
            return;
        }

        if (expected instanceof Expr.Logical) {
            compareExpr((Expr.Logical) expected, (Expr.Logical) actual);
            return;
        }
        if (expected instanceof Expr.Call) {
            compareExpr((Expr.Call) expected, (Expr.Call) actual);
            return;
        }
        if (expected instanceof Expr.Get) {
            compareExpr((Expr.Get) expected, (Expr.Get) actual);
            return;
        }
        if (expected instanceof Expr.Set) {
            compareExpr((Expr.Set) expected, (Expr.Set) actual);
            return;
        }
        if (expected instanceof Expr.This) {
            compareExpr((Expr.This) expected, (Expr.This) actual);
            return;
        }
        fail("unexpected Expr class");
    }

    public static void compareExpr(Expr.This expected, Expr.This actual) {
        compareToken(expected.keyword, actual.keyword);
    }

    public static void compareExpr(Expr.Set expected, Expr.Set actual) {
        compareToken(expected.name, actual.name);
        compareExpr(expected.object, actual.object);
        compareExpr(expected.value, actual.value);
    }

    public static void compareExpr(Expr.Get expected, Expr.Get actual) {
        compareExpr(expected.object, actual.object);
        compareToken(expected.name, actual.name);
    }

    public static void compareStmt(Stmt.Return expected, Stmt.Return actual) {
        compareToken(expected.keyword, actual.keyword);
        compareExpr(expected.value, actual.value);
    }

    public static void compareExpr(Expr.Call expected, Expr.Call actual) {
        compareToken(expected.paren, actual.paren);
        compareExpr(expected.callee, actual.callee);
        assertEquals(expected.arguments.size(), actual.arguments.size());
        for (var i = 0; i < expected.arguments.size(); i++) {
            compareExpr(expected.arguments.get(i), actual.arguments.get(i));
        }
    }

    public static void compareExpr(Expr.Logical expected, Expr.Logical actual) {
        compareToken(expected.operator, actual.operator);
        compareExpr(expected.left, actual.left);
        compareExpr(expected.right, actual.right);
    }


    public static void compareExpr(Expr.Assignment expected, Expr.Assignment actual) {
        compareToken(expected.name, actual.name);
        compareExpr(expected.expression, actual.expression);

    }

    public static void compareExpr(Expr.Unary expected, Expr.Unary actual) {
        compareToken(expected.operator, actual.operator);
        compareExpr(expected.right, actual.right);
    }

    public static void compareExpr(Expr.Binary expected, Expr.Binary actual) {
        compareToken(expected.operator, actual.operator);
        compareExpr(expected.left, actual.left);
        compareExpr(expected.right, actual.right);
    }

    public static void compareExpr(Expr.Variable expected, Expr.Variable actual) {
        compareToken(expected.name, actual.name);
    }

    public static void compareExpr(Expr.Literal expected, Expr.Literal actual) {
        assertEquals(expected.value, actual.value);
    }

    public static void compareExpr(Expr.Grouping expected, Expr.Grouping actual) {
        compareExpr(expected.expression, actual.expression);
    }

    public static void compareToken(Token expected, Token actual) {
        assertEquals(expected.type, actual.type, "two different name types");
        assertEquals(expected.lexeme, actual.lexeme, "two different name lexemes");
        assertEquals(expected.literal, actual.literal, "two different name literals");
        assertEquals(expected.line, actual.line, "two different name lines");
    }
}
