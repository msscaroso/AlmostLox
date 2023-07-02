package crafting.interpreters.core.base;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public final Map<String, Object> values;
    public final Environment enclosing;

    public Environment() {
        values = new HashMap<>();
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        values = new HashMap<>();
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object readVariableValue(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (enclosing != null) {
            return enclosing.readVariableValue(name);
        }
        throw new RuntimeException("Variable not declared");
    }

    public void assign(String name, Object value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeException("Variable not declared");
    }
}
