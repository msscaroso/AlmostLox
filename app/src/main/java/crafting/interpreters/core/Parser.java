package crafting.interpreters.core;

import crafting.interpreters.core.base.Expr;
import crafting.interpreters.core.base.Stmt;
import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.base.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static crafting.interpreters.core.base.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    private final int lastIdx;

    private final List<TokenType> comparisonTokens = new ArrayList<>(
            Arrays.asList(
                    TokenType.GREATER,
                    TokenType.GREATER_EQUAL,
                    TokenType.LESS,
                    TokenType.LESS_EQUAL
            )
    );
    private final List<TokenType> equalityTokens = new ArrayList<>(
            Arrays.asList(
                    TokenType.EQUAL_EQUAL,
                    TokenType.BANG_EQUAL
            )
    );

    public static class ParserError extends RuntimeException {
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        lastIdx = tokens.size() - 1;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (beforeEnd()) {
            Stmt stmt = declaration();
            statements.add(stmt);
            if (beforeEnd() && (tokens.get(current).type == TokenType.EOF) || (current < lastIdx && tokens.get(current + 1).type == TokenType.EOF)) {
                return statements;
            }
        }

        return statements;
    }


// grammar:
// program        → declaration* EOF ;
// declaration    → classDecl | funDecl | varDecl | statement ;
// statement      → exprStmt | ifStmt | printStmt | block | whileStmt | forStmt | returnStmt;
// classDecl      → "class" IDENTIFIER "{" function* "}" ;
// funDecl        → "fun" function ;
// function       → IDENTIFIER "(" parameters? ")" block ;
// parameters     → IDENTIFIER ( "," IDENTIFIER )* ;
// varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
// forStmt        → "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
// whileStmt      → "while" "(" expression ")" statement ;
// ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;
// exprStmt       → expression ";" ;
// printStmt      → "print" expression ";" ;
// returnStmt     → "return" expression? ";" ;
// block          → "{" declaration* "}" ;
// expression     → assignment ;
// assignment     → ( call "." )? IDENTIFIER "=" assignment | logic_or ;
// logic_or       → logic_and ( "or" logic_and )* ;
// logic_and      → equality ( "and" equality )* ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           → factor ( ( "-" | "+" ) factor )* ;
// factor         → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary | call ;
// call           → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
// arguments      → expression ( "," expression )* ;
// primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;


    private Stmt declaration() {
        if (match(CLASS) != null) {
            return classDecl();
        }
        if (match(VAR) != null) {
            return varDecl();
        }
        if (match(FUN) != null) {
            return funDecl();
        }
        return statement();
    }

    private Stmt classDecl() {
        var name = consume(IDENTIFIER);
        consume(LEFT_BRACE);
        List<Stmt.Function> methods = new ArrayList<>();
        while (match(RIGHT_BRACE) == null) {
            methods.add((Stmt.Function) funDecl());
        }
        return new Stmt.Class(name, methods);
    }

    private Stmt funDecl() {
        var funName = consume(IDENTIFIER);
        consume(LEFT_PAREN);
        List<Token> params = null;
        if (match(RIGHT_PAREN) == null) {
            params = parameters();
            consume(RIGHT_PAREN);
        }
        if (params == null) params = new ArrayList<>();
        consume(LEFT_BRACE);
        var body = block();
        return new Stmt.Function(
                funName,
                params,
                body
        );
    }

    private List<Token> parameters() {
        List<Token> params = new ArrayList<>();
        do {
            params.add(consume(IDENTIFIER));
        } while (match(COMMA) != null);
        return params;
    }

    private Stmt varDecl() {
        var varName = consume(IDENTIFIER);
        Expr initializer = null;
        if (match(EQUAL) != null) {
            initializer = expression();
        }
        consume(SEMICOLON);
        return new Stmt.VarDcl(varName, initializer);
    }

    private Stmt statement() {

        if (match(IF) != null) {
            return ifStmt();
        }
        if (match(PRINT) != null) {
            return printStmt();
        }
        if (match(LEFT_BRACE) != null) {
            return new Stmt.Block(block());
        }
        if (match(WHILE) != null) {
            return whileStmt();
        }
        if (match(FOR) != null) {
            return forStmt();
        }
        if (match(RETURN) != null) {
            return returnStmt();
        }
        return exprStmt();
    }

    private Stmt returnStmt() {
        Token keyword = tokens.get(current - 1);
        if (match(SEMICOLON) != null) {
            return new Stmt.Return(keyword, null);
        }
        Expr expr = expression();
        consume(SEMICOLON);
        return new Stmt.Return(keyword, expr);
    }

    private Stmt forStmt() {
        consume(LEFT_PAREN);
        Stmt initializer;
        Expr condition = null;
        Expr increment = null;

        if (match(VAR) != null) {
            initializer = varDecl();
        } else if (match(SEMICOLON) != null) {
            initializer = null;
        } else {
            initializer = exprStmt();
        }

        if (match(SEMICOLON) == null) {
            condition = expression();
            consume(SEMICOLON);
        }

        if (match(RIGHT_PAREN) == null) {
            increment = expression();
            consume(RIGHT_PAREN);
        }

        Stmt body = statement();
        return forToWhile(initializer, condition, increment, body);
    }

    Stmt forToWhile(Stmt init, Expr cond, Expr inc, Stmt body) {
        if (cond == null) {
            cond = new Expr.Literal(true);
        }
        List<Stmt> stmts = new ArrayList<>();
        List<Stmt> bodyStmts = new ArrayList<>();
        if (init != null) {
            stmts.add(init);
        }
        bodyStmts.add(body);
        if (inc != null) {
            bodyStmts.add(new Stmt.ExprStmt(inc));
        }
        stmts.add(new Stmt.While(cond, new Stmt.Block(bodyStmts)));
        return new Stmt.Block(stmts);
    }

    private Stmt whileStmt() {
        consume(LEFT_PAREN);
        Expr expr = expression();
        consume(RIGHT_PAREN);
        Stmt stmt = statement();
        return new Stmt.While(expr, stmt);
    }

    private Stmt ifStmt() {
        consume(LEFT_PAREN);
        Expr condition = expression();
        consume(RIGHT_PAREN);
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE) != null) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> stmts = new ArrayList<>();
        while (beforeEnd() && tokens.get(current).type != RIGHT_BRACE) {
            stmts.add(declaration());
        }
        consume(RIGHT_BRACE);
        return stmts;
    }

    private Stmt exprStmt() {
        var expr = expression();
        consume(TokenType.SEMICOLON);
        return new Stmt.ExprStmt(expr);
    }

    private Stmt printStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON);
        return new Stmt.Print(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        var expr = logic_or();

        if (match(EQUAL) != null) {
            if (expr instanceof Expr.Variable) {
                var rValue = expression();
                return new Expr.Assignment(
                        ((Expr.Variable) expr).name,
                        rValue
                );
            } else if (expr instanceof Expr.Get) {
                var rValue = expression();
                return new Expr.Set(
                        ((Expr.Get) expr).object,
                        ((Expr.Get) expr).name,
                        rValue

                );
            }
            System.err.println("Can only assign to a variable");
            throw new ParserError();
        }
        return expr;
    }

    private Expr logic_or() {
        var expr = logical_and();
        Token tk;
        while ((tk = match(OR)) != null) {
            var right = logical_and();
            expr = new Expr.Logical(
                    expr,
                    tk,
                    right
            );
        }
        return expr;
    }

    private Expr logical_and() {
        var expr = equality();
        Token tk;
        while ((tk = match(AND)) != null) {
            Expr right = equality();
            expr = new Expr.Logical(
                    expr,
                    tk,
                    right
            );
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        if (!beforeEnd()) {
            return expr;
        }
        Token tk = tokens.get(current);
        while (equalityTokens.contains(tk.type)) {
            current++;
            Expr right = comparison();
            expr = new Expr.Binary(
                    expr, right,
                    tk
            );
            if (!beforeEnd()) {
                break;
            }
            tk = tokens.get(current);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        if (!beforeEnd()) {
            return expr;
        }
        Token tk = tokens.get(current);

        while (comparisonTokens.contains(tk.type)) {
            current++;
            Expr right = term();
            expr = new Expr.Binary(
                    expr,
                    right,
                    tk
            );
            if (!beforeEnd()) {
                break;
            }
            tk = tokens.get(current);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        if (!beforeEnd()) {
            return expr;
        }
        Token tk = tokens.get(current);
        while (tk.type == TokenType.MINUS || tk.type == TokenType.PLUS) {
            current++;
            Expr right = factor();
            expr = new Expr.Binary(expr, right, tk);
            if (!beforeEnd()) {
                break;
            }
            tk = tokens.get(current);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        Token tk;
        while ((tk = match(TokenType.SLASH, TokenType.STAR)) != null) {
            Expr right = unary();
            expr = new Expr.Binary(expr, right, tk);
            if (!beforeEnd()) {
                break;
            }
        }
        return expr;
    }

    private Expr unary() {
        Token tk;
        if ((tk = match(TokenType.BANG, TokenType.MINUS)) != null) {
            Expr expr = unary();
            return new Expr.Unary(tk, expr);
        }
        return call();
    }

    private Expr call() {
        var expr = primary();
        Token tk;
        while ((tk = match(LEFT_PAREN, DOT)) != null) {
            // it's call
            if (tk.type == LEFT_PAREN) {
                Token paren;

                if ((paren = match(RIGHT_PAREN)) != null) {
                    expr = new Expr.Call(expr, paren, new ArrayList<>());
                    continue;
                }
                List<Expr> args = arguments();
                if (args.size() > 255) {
                    System.err.println("Can't have more than 255 arguments");
                }
                paren = consume(RIGHT_PAREN);
                expr = new Expr.Call(expr, paren, args);
                continue;
            }
            // otherwise, we're getting a property
            var identifier = consume(IDENTIFIER);
            expr = new Expr.Get(expr, identifier);
        }
        return expr;
    }

    private List<Expr> arguments() {
        List<Expr> args = new ArrayList<>();
        Expr expr;
        do {
            expr = expression();
            args.add(expr);
        } while (match(COMMA) != null);
        return args;
    }

    private Expr primary() {
        Token tk = tokens.get(current);
        if (tk.type == TokenType.NUMBER || tk.type == FALSE || tk.type == TRUE || tk.type == STRING) {
            current++;
            return new Expr.Literal(tk.literal);
        }
        if (tk.type == TokenType.NIL) {
            current++;
            return new Expr.Literal(null);
        }

        if (tk.type == TokenType.LEFT_PAREN) {
            current++;
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN);
            return new Expr.Grouping(expr);
        }
        if (tk.type == IDENTIFIER) {
            current++;
            return new Expr.Variable(tk);
        }
        if(tk.type == THIS){
            current++;
            return new Expr.This(tk);
        }
        throw new ParserError();
    }

    private boolean beforeEnd() {
        return current <= lastIdx;
    }

    private Token match(TokenType... tts) {
        // if the current token matches one of 'tts', then return the current token and advance.
        // otherwise, return null
        if (!beforeEnd()) {
            return null;
        }
        var token = tokens.get(current);
        for (var tt : tts) {
            if (token.type == tt) {
                current++;
                return token;
            }
        }
        return null;
    }

    private Token consume(TokenType tt) {
        if (!beforeEnd()) {
            throw new ParserError();
        }
        var tk = tokens.get(current);
        if (tk.type != tt) {
            throw new ParserError();
        }
        current++;
        return tk;
    }

}
