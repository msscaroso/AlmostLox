package crafting.interpreters;

import crafting.interpreters.core.Interpreter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoxTest {
    static class CapturePrint extends Interpreter.CapturePrint {

    }


    @Test
    void initReturnInvalid() {
        String src = """
                class Foo{
                    init(){
                        return 1;
                    }
                }
                Foo();
                """;
        try {
            new Lox().run(src);
            fail("init should return null");
        } catch (RuntimeException ignored){

        }
    }

    @Test
    void init() {
        String src = """
                class Foo{
                    init(x){
                     this.abc = x;
                     return nil;
                    }
                    p(){
                        print this.abc;
                    }
                }
                var f = Foo(2);
                f.p();
                """;
        var actual = runAndCapture(src);
        assertEquals(2., actual);
    }

    @Test
    void thisKeepsReferenceToInstance() {
        String src = """
                class Foo{
                    p(){
                        print this.abc;
                    }
                }
                var f = Foo();
                var g = Foo();
                f.abc = 3;
                g.abc = 10;
                g.o = f.p;
                g.abc = 20;
                g.o();
                """;
        var actual = runAndCapture(src);
        assertEquals(3., actual);
    }

    @Test
    void setThisProperty() {
        String src = """
                class Foo{
                    a(){
                        this.abc = 4;
                    }
                    p(){
                        print this.abc;
                    }
                }
                var f = Foo();
                f.a();
                f.p();
                """;
        var actual = runAndCapture(src);
        assertEquals(4., actual);
    }

    @Test
    void useThis() {
        String src = """
                class Foo{
                    p(){
                        print this.abc;
                    }
                }
                var f = Foo();
                f.abc = 5;
                f.p();
                """;
        var actual = runAndCapture(src);
        assertEquals(5., actual);
    }

    @Test
    void methodCall() {
        String src = """
                class Foo {
                    bar(){
                        print 123;
                    }
                }
                var f = Foo();
                f.bar();
                """;
        var actual = runAndCapture(src);
        assertEquals(123., actual);
    }

    @Test
    void setAndGetProperty() {
        String src = """
                class Foo{}
                var x = Foo();
                x.y = 15;
                print x.y;
                """;
        var actual = runAndCapture(src);
        assertEquals(15., actual);
    }

    @Test
    void setPropertyOfNonInstance() {
        String src = """
                var x = 1;
                x.y = 2;
                """;
        try {
            new Lox().run(src);
            fail("Can only set property of a class instance");
        } catch (RuntimeException ignored) {
        }


    }

    @Test
    void getPropertyFromNonInstance() {
        String src = """
                var x = 1;
                x.y;
                """;
        try {
            new Lox().run(src);
            fail("Can only get property from a class instance");
        } catch (RuntimeException ignored) {
        }

    }

    @Test
    void createClassInstance() {
        String src = """
                class Foo {}
                var x = Foo();
                print x;
                """;
        var actual = runAndCapture(src);
        assertEquals("Foo instance", actual);
    }

    @Test
    void printClass() {
        String src = """
                class abc{}
                print abc;
                """;
        var actual = runAndCapture(src);
        assertEquals("abc", actual);
    }

    @Test
    void printString() {
        String src = """
                    var x = "Hello world";
                    print x;
                """;
        var actual = runAndCapture(src);
        assertEquals("Hello world", actual);
    }

    @Test
    void closure() {
        String src = """
                {
                    var a = 1;
                    fun foo(){
                       print a;
                    }
                    foo();
                }
                """;

        var actual = runAndCapture(src);
        assertEquals(1., (Double) actual);

    }

    @Test
    void returnNil() {
        String src = """
                fun a(){ return;}
                print a();
                """;

        var actual = runAndCapture(src);
        assertNull(actual);
    }

    @Test
    void returnValue() {
        String src = """
                fun a(){ return 100;}
                print a();
                """;

        var actual = runAndCapture(src);
        assertEquals(100., (Double) actual);
    }

    @Test
    void arity() {
        String src = """
                fun foo(a){}
                foo(1, 2, 3);
                """;
        try {
            new Lox().run(src);
        } catch (RuntimeException err) {
            assertEquals("Wrong number of arguments", err.getMessage());
        }


    }

    @Test
    void callFunctionFromOtherScope() {
        String src = """
                 
                if (true){
                    fun printSum (a, b){
                        print a + b;
                    }
                }
                printSum(1, 10);
                """;

        try {
            new Lox().run(src);
            fail("should fail trying to access a non declared variable");
        } catch (RuntimeException err) {
            assertEquals("Variable not declared", err.getMessage());
        }
    }

    @Test
    void functionDeclInsideBlock() {
        String src = """
                 
                if (true){
                    fun printSum (a, b){
                        print a + b;
                    }
                    printSum(1, 10);
                }

                """;
        var actual = runAndCapture(src);
        assertEquals(11., (Double) actual);
    }

    @Test
    void functionDecl() {
        String src = """
                fun printSum (a, b){
                    print a + b;
                }
                printSum(1, 10);
                """;
        var actual = runAndCapture(src);
        assertEquals(11., (Double) actual);
    }

    @Test
    void nativeFn() {
        String src = """
                var t = clock();
                if (t > 1){
                    print 123;
                }
                """;
        var actual = runAndCapture(src);
        assertEquals(123., (Double) actual);
    }

    @Test
    void notCallable() {
        String source = """
                var x = 1;
                x();
                """;
        try {
            new Lox().run(source);
            fail("Should have thrown an exception. 1 is not callable.");
        } catch (RuntimeException err) {
            assertEquals(err.getMessage(), "1.0 is not callable");
        }
    }

    @Test
    void forTest() {
        String source = """
                                
                    var x = 100;
                    for (var y = 0; y<200; y=y+1){
                        x = x + 1;
                    }
                    print x;
                """;
        var actual = runAndCapture(source);
        assertEquals(300., (Double) actual);
    }

    @Test
    void whileTest() {
        String source = """
                    var i = 0;
                    while (i <= 3){
                        i = i + 1;
                    }
                    print i;
                """;
        var actual = runAndCapture(source);
        assertEquals(4., (Double) actual);
    }

    @Test
    void andOr() {
        String source = """
                    var x = false or 11;
                    var y = 1 and 5;
                    var z = 5 and 100 or -500;
                    print x + y + z;
                """;
        var actual = runAndCapture(source);
        assertEquals(116., (Double) actual);
    }

    @Test
    void multipleIfs() {
        String source = """
                                
                    var x = 2;
                    if (x != 1) {
                        if (x == 2) {
                            x = 5;
                        }
                    }
                    if (x == 5) {
                        print 500;
                    }
                """;
        var actual = runAndCapture(source);
        assertEquals(500., (Double) actual);
    }

    @Test
    void ifElse() {
        String source = """
                                
                    var x = 2;
                    if (x == 1) {
                        print x;
                    }
                    else {
                        print 5;
                    }
                """;
        var actual = runAndCapture(source);
        assertEquals(5., (Double) actual);
    }

    @Test
    void ifThen() {
        String source = """
                                
                    var x = 1;
                    if (x == 1) {
                        print x;
                    }
                """;
        var actual = runAndCapture(source);
        assertEquals(1., (Double) actual);
    }

    @Test
    void blockScope() {
        String source = """
                 var abc = 15;
                 {
                    var abc = 3;
                 }
                 print abc;
                """;

        var actual = runAndCapture(source);
        assertEquals(15., (Double) actual);

    }

    @Test
    void block() {
        String source = """
                 var abc = 15;
                 {
                    var x = abc*3;
                    print x;
                 }
                """;

        var actual = runAndCapture(source);
        assertEquals(45., (Double) actual);
    }

    @Test
    void assignAndPrint() {
        String source = """
                 var abc = 15;
                 abc = 2*abc;
                 print abc;
                """;

        var actual = runAndCapture(source);
        assertEquals(30., (Double) actual);
    }

    @Test
    void declareAndPrint() {


        String source = """
                 var abc = 15;
                 var c = 2*abc;
                 print (-10 + 20)*c;
                """;
        var actual = runAndCapture(source);
        assertEquals(300., (Double) actual);
    }

    @Test
    void testSumDivMulSub() {
        String source = "print (1 - 1 + 1) * 4 / (-1 * 2 +4);";
        var actual = runAndCapture(source);
        assertEquals(2., (Double) actual);
    }

    @Test
    void testComparison() {
        String source = "print (-1 + 7) / 2 == 3;";
        var actual = runAndCapture(source);
        assertEquals(true, actual);
    }

    @Test
    void multipleStatements() {
        String source = """
                    1 + 1 / 1;
                    print 100*3/20;
                """;
        var actual = runAndCapture(source);
        assertEquals(15., (Double) actual);
    }

    Object runAndCapture(String source) {
        var cp = new CapturePrint();
        new Lox().run(source, cp);
        return cp.capturedValue;
    }
}
