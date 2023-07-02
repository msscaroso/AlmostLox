package crafting.interpreters.core;

import crafting.interpreters.Lox;
import crafting.interpreters.core.base.*;
import crafting.interpreters.core.base.LoxCallable.LoxFunction;
import crafting.interpreters.core.base.LoxCallable.LoxClass;
import crafting.interpreters.core.base.LoxCallable.LoxInstance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public static class ReturnException extends RuntimeException {
        public final Object value;

        public ReturnException(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

    public static class CapturePrint {
        public Object capturedValue = null;
        public boolean captured = false;
    }


    public final Environment globals = new Environment();
    public Environment environment = globals;
    public final List<Stmt> statements;
    public boolean hadError;

    public final CapturePrint cp;

    private void defineNativeFunctions() {
        var clock = new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }
        };
        globals.define("clock", clock);
    }

    public Interpreter(List<Stmt> statements, CapturePrint cp) {
        this.statements = statements;
        this.cp = cp;
        defineNativeFunctions();
    }

    public void interpret() {
        for (var stmt : statements) {
            stmt.accept(this);
        }
    }

    private final List<TokenType> numberRequired = new ArrayList<>(Arrays.asList(
            TokenType.PLUS,
            TokenType.MINUS,
            TokenType.SLASH,
            TokenType.STAR,
            TokenType.LESS_EQUAL,
            TokenType.LESS,
            TokenType.GREATER,
            TokenType.GREATER_EQUAL
    ));


    @Override
    public Void visitPrint(Stmt.Print stmt) {
        Object val = stmt.expression.accept(this);
        if (val != null) {
            System.out.println(val);
        } else {
            System.out.println("nil");
        }
        if (this.cp != null) {
            this.cp.captured = true;
            this.cp.capturedValue = null;
            if (val instanceof Double || val instanceof String || val instanceof Boolean) {
                this.cp.capturedValue = val;
            } else if (val != null) {
                this.cp.capturedValue = val.toString();
            }
        }
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {

        Object leftValue = expr.left.accept(this);

        Object rightValue = expr.right.accept(this);

        if (numberRequired.contains(expr.operator.type)) {
            checkNumber(expr.operator, leftValue);
            checkNumber(expr.operator, rightValue);
        }
        return switch (expr.operator.type) {
            case PLUS -> (Double) leftValue + (Double) rightValue;
            case MINUS -> (Double) leftValue - (Double) rightValue;
            case SLASH -> (Double) leftValue / (Double) rightValue;
            case STAR -> (Double) leftValue * (Double) rightValue;
            case BANG_EQUAL -> !isEqual(leftValue, rightValue);
            case EQUAL_EQUAL -> isEqual(leftValue, rightValue);
            case LESS_EQUAL -> (Double) leftValue <= (Double) rightValue;
            case LESS -> (Double) leftValue < (Double) rightValue;
            case GREATER -> (Double) leftValue > (Double) rightValue;
            case GREATER_EQUAL -> (Double) leftValue >= (Double) rightValue;
            default -> throw new RuntimeException(String.format("Unknown operator %s", expr.operator.lexeme));
        };
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object rightValue = expr.right.accept(this);

        switch (expr.operator.type) {
            case MINUS -> {
                checkNumber(expr.operator, rightValue);
                return -(Double) rightValue;
            }
            case BANG -> {
                return !isTruthy(rightValue);
            }
            default -> throw new RuntimeException(String.format("Unknown operator %s", expr.operator.lexeme));
        }
    }

    @Override
    public Object visitLiteral(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Void visitAssignment(Expr.Assignment assignment) {
        Object value = assignment.expression.accept(this);
        this.environment.assign(assignment.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitVarDcl(Stmt.VarDcl varDcl) {
        Object value = varDcl.expressionInitializer.accept(this);
        this.environment.define(varDcl.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariable(Expr.Variable variable) {
        return this.environment.readVariableValue(variable.name.lexeme);
    }

    @Override
    public Void visitClass(Stmt.Class klass) {
        var methods = new HashMap<String, LoxFunction>();
        for (var methodStmt: klass.methods){
            var method = new LoxFunction(methodStmt);
            methods.put(methodStmt.name.lexeme, method);
        }
        LoxClass c = new LoxClass(klass.name.lexeme, methods);
        environment.define(klass.name.lexeme, c);
        return null;
    }

    @Override
    public Void visitFunction(Stmt.Function function) {
        var f = new LoxFunction(function);
        environment.define(function.name.lexeme, f);
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While whileStmt) {
        while (isTruthy(whileStmt.condition.accept(this))) {
            whileStmt.statement.accept(this);
        }
        return null;
    }

    @Override
    public Object visitLogical(Expr.Logical logical) {
        Object left = logical.left.accept(this);
        if (logical.operator.type == TokenType.AND) {
            if (!isTruthy(logical.left)) {
                return left;
            }
            return logical.right.accept(this);
        }
        if (isTruthy(left)) {
            return left;
        }
        return logical.right.accept(this);
    }

    @Override
    public Void visitIf(Stmt.If ifStmt) {
        Object conditionValue = ifStmt.condition.accept(this);
        if (isTruthy(conditionValue)) {
            ifStmt.thenBranch.accept(this);
        } else if (ifStmt.elseBranch != null) {
            ifStmt.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block block) {
        executeBlock(block.statements, new Environment(this.environment));
        return null;
    }

    public void executeBlock(
            List<Stmt> statements,
            Environment environment
    ) {
        Environment previous = this.environment;

        try {
            this.environment = environment;
            for (Stmt stmt : statements) {
                stmt.accept(this);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitCall(Expr.Call call) {
        Object callee = call.callee.accept(this);
        List<Object> args = new ArrayList<>();
        for (Expr arg : call.arguments) {
            args.add(arg.accept(this));
        }
        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeException(String.format("%s is not callable", callee.toString()));
        }
        if (function.arity() != args.size()) {
            throw new RuntimeException("Wrong number of arguments");
        }
        return function.call(this, args);
    }


    @Override
    public Object visitGet(Expr.Get get) {
        Object instance = get.object.accept(this);
        checkIsInstance(instance);
        return ((LoxInstance) instance).get(get.name.lexeme);
    }


    @Override
    public Object visitSet(Expr.Set set) {
        Object instance = set.object.accept(this);
        checkIsInstance(instance);
        Object value = set.value.accept(this);
        ((LoxInstance) instance).set(set.name.lexeme, value);
        return value;
    }

    @Override
    public Object visitThis(Expr.This expr) {
        return environment.readVariableValue("this");
    }

    private void checkIsInstance(Object instance){
        if (!(instance instanceof LoxCallable.LoxInstance)){
            throw new RuntimeException(String.format("%s is not an instance of a class", instance.toString()));
        }
    }
    @Override
    public Void visitReturn(Stmt.Return ret) {
        Object value = null;
        if (ret.value != null) value = ret.value.accept(this);
        throw new ReturnException(value);
    }

    void checkNumber(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        String msg = "Number was expected";
        error(operator.line, msg);
        throw new RuntimeException(msg);
    }

    static Boolean isEqual(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return leftValue == null && rightValue == null;
        }
        return leftValue.equals(rightValue);
    }

    static Boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    void error(int line, String message) {
        System.out.printf("Error in line %d. Error: %s", line, message);
        hadError = true;
    }
}
