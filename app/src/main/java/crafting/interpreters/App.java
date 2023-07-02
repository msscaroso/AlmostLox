package crafting.interpreters;

import java.io.IOException;


public class App {

    public static void main(String[] args) throws IOException {
        Lox lox = new Lox();
        lox.runFile("app.lox");
    }
}
