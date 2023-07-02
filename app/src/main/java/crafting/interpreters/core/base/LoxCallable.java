package crafting.interpreters.core.base;

import crafting.interpreters.Lox;
import crafting.interpreters.core.Interpreter;
import crafting.interpreters.core.Interpreter.ReturnException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);

    class LoxFunction implements LoxCallable {
        private final Stmt.Function declaration;
        private final LoxInstance thisInstance;

        public LoxFunction(Stmt.Function declaration) {
            this.declaration = declaration;
            this.thisInstance = null;
        }

        public LoxFunction(Stmt.Function declaration, LoxInstance thisInstance) {
            this.declaration = declaration;
            this.thisInstance = thisInstance;
        }

        @Override
        public int arity() {
            return declaration.params.size();
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            var enclosingEnv = interpreter.environment;
            var functionEnv = new Environment(enclosingEnv);
            for (var i = 0; i < arguments.size(); i++) {
                functionEnv.define(declaration.params.get(i).lexeme, arguments.get(i));
            }
            if (this.thisInstance != null) {
                functionEnv.define("this", thisInstance);
            }
            try {
                interpreter.executeBlock(declaration.body, functionEnv);
            } catch (ReturnException ret) {
                return ret.value;
            }
            return null;
        }
    }

    class LoxClass implements LoxCallable {

        private final String name;
        private final HashMap<String, LoxFunction> methods;

        public LoxClass(String name, HashMap<String, LoxFunction> methods) {
            this.name = name;
            this.methods = methods;
        }

        @Override
        public int arity() {
            LoxFunction init = findMethod("init");
            if (init != null) {
                return init.arity();
            }
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            var instance = new LoxInstance(this);
            LoxFunction init = instance.getInit();
            if (init != null) {
                Object ret = init.call(interpreter, arguments);
                if (ret != null) {
                    throw new RuntimeException("init should return nil");
                }
            }
            return instance;
        }

        @Override
        public String toString() {
            return name;
        }

        LoxFunction findMethod(String name) {
            if (!methods.containsKey(name)) {
                return null;
            }
            return methods.get(name);
        }

    }

    class LoxInstance {
        // This is the internal representation of a class instance in the Lox interpreter.
        // When we invoke `var x = Klass()`, x will hold a LoxInstance produced by the 'Klass' call.
        private final LoxClass klass;

        private final HashMap<String, Object> properties = new HashMap<>();

        public LoxInstance(LoxClass klass) {
            this.klass = klass;
        }

        public LoxFunction getInit() {
            return getMethod("init");
        }

        private LoxFunction getMethod(String name) {
            // bind the function to the instance before returning a new instance of LoxFunction
            LoxFunction method = klass.findMethod(name);
            if (method != null) {
                return new LoxFunction(method.declaration, this);
            }
            return null;
        }

        public Object get(String name) {
            if (properties.containsKey(name)) {
                return properties.get(name);
            }
            ;
            LoxFunction method = getMethod(name);
            if (method != null){
                return method;
            }
            throw new RuntimeException(toString() + " doesn't contain the property " + name);
        }

        public void set(String name, Object value) {
            if (Objects.equals(name, "this")) {
                throw new RuntimeException("Property can't be named 'this'");
            }
            properties.put(name, value);
        }

        @Override
        public String toString() {
            return klass.toString() + " instance";
        }
    }

}

