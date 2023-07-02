package crafting.interpreters.core.base;

import java.util.List;

public abstract class Expr {

    public interface Visitor<R> {
        R visitBinary(Binary expr);

        R visitGrouping(Grouping expr);

        R visitUnary(Unary expr);

        R visitLiteral(Literal literal);

        R visitVariable(Variable variable);

        R visitAssignment(Assignment assignment);

        R visitLogical(Logical logical);

        R visitCall(Call call);

        R visitGet(Get get);
        R visitSet(Set set);
        R visitThis(This expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    static public class Binary extends Expr {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }

        public Binary(Expr left, Expr right, Token operator) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    static public class Literal extends Expr {
        public Literal(Object value) {
            this.value = value;
        }

        public final Object value;

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    static public class Unary extends Expr {
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        public final Token operator;
        public final Expr right;

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }
    }

    static public class Grouping extends Expr {

        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }

    }

    static public class Variable extends Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariable(this);
        }
    }

    static public class Get extends Expr {
        public final Expr object; // the thing that we are getting the property from
        public final Token name; // the name of the property

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }


        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGet(this);
        }
    }

    static public class Assignment extends Expr {
        public final Token name;
        public final Expr expression;

        public Assignment(Token name, Expr expression) {
            this.name = name;
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignment(this);
        }
    }

    static public class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogical(this);
        }
    }

    static public class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren; // the right parenthesis of the call
            this.arguments = arguments;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCall(this);
        }
    }

    static public class Set extends Expr {

        public final Expr object;
        public final Token name;
        public final Expr value;

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSet(this);
        }

    }
    static public class This extends Expr {
        public final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThis(this);
        }
    }
}
