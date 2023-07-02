package crafting.interpreters.core.base;

public class Char {
    public static boolean isAlpha(char x) {
        return ('a' <= x && x <= 'z') || ('A' <= x && x <= 'Z') || x == '_';
    }

    public static boolean isNumber(char x) {
        return '0' <= x && x <= '9';
    }

    public static boolean isAlphaNumeric(char x) {
        return isAlpha(x) || isNumber(x);
    }

}
