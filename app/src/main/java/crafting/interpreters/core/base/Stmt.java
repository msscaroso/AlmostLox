package crafting.interpreters.core.base;

import java.util.List;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitPrint(Stmt.Print stmt);

        R visitExprStmt(Stmt.ExprStmt exprStmt);

        R visitVarDcl(Stmt.VarDcl varDcl);

        R visitBlock(Stmt.Block block);

        R visitIf(Stmt.If ifStmt);

        R visitWhile(Stmt.While whileStmt);

        R visitFunction(Stmt.Function function);
        R visitReturn(Stmt.Return ret);
        R visitClass(Stmt.Class klass);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrint(this);
        }
    }

    public static class ExprStmt extends Stmt {
        public final Expr expression;

        public ExprStmt(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStmt(this);
        }

    }

    public static class VarDcl extends Stmt {
        public final Token name;
        public final Expr expressionInitializer;

        public VarDcl(Token name, Expr expressionInitializer) {
            this.name = name;
            this.expressionInitializer = expressionInitializer;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDcl(this);
        }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlock(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIf(this);
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt statement;

        public While(Expr condition, Stmt statement) {
            this.condition = condition;
            this.statement = statement;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhile(this);
        }
    }

    public static class Function extends Stmt {
        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunction(this);
        }
    }

    static public class Return extends Stmt {
        public final Token keyword;
        public final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturn(this);
        }
    }
    static public class Class extends Stmt {
        public final Token name;
        public final List<Stmt.Function> methods;
        public Class(Token name, List<Stmt.Function> methods){
            this.name = name;
            this.methods = methods;
        }
        public <R> R accept(Visitor<R> visitor){
            return visitor.visitClass(this);
        }
    }
}
