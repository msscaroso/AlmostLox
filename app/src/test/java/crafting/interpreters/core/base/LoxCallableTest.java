package crafting.interpreters.core.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static crafting.interpreters.core.base.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

import crafting.interpreters.core.base.LoxCallable.LoxInstance;
import crafting.interpreters.core.base.LoxCallable.LoxClass;

import java.util.ArrayList;
import java.util.HashMap;

class LoxCallableTest {
    private LoxInstance instance;

    @BeforeEach
    void setUp() {
        var klass = new LoxClass("ABC", new HashMap<>());
        instance = new LoxInstance(klass);
    }

    @Test
    void testGetUndefined() {

        try {
            instance.get("x");
            fail("calling get with undefined variable should fail");
        } catch (RuntimeException ignored) {

        }
    }

    @Test
    void testSetAndGet() {
        instance.set("x", 123);
        assertEquals(instance.get("x"), 123);
    }
    @Test
    void testSetThis(){
        try{
            instance.set("this", 123);
            fail("Property can't be named 'this'");
        } catch (RuntimeException ignored){}
    }
}
