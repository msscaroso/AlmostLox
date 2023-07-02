package crafting.interpreters;

import crafting.interpreters.core.base.Token;
import crafting.interpreters.core.Interpreter;
import crafting.interpreters.core.Parser;
import crafting.interpreters.core.Scanner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    public boolean hadError = false;

    public void runFile(String fileName) throws IOException {
        System.out.println("Running file: " + fileName);
        Path path = Paths.get(fileName);

        byte[] bytes = Files.readAllBytes(path);

        System.out.println(path.toAbsolutePath());
        run(new String(bytes, StandardCharsets.UTF_8));
    }

    public void run(String source){
        run(source, null);
    }

    public void run(String source, Interpreter.CapturePrint cp) {
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        if (hadError) throw new Scanner.ScannerError();
        Parser parser = new Parser(tokens);

        var stmt = parser.parse();
        Interpreter interpreter = new Interpreter(stmt, cp);
        interpreter.interpret();
    }

}
