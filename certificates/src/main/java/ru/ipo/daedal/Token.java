package ru.ipo.daedal;

import static ru.ipo.daedal.TokenType.*;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 21:42.
 */
public class Token {

    public static Token text(char c) {
        return new Token(Text, null, c);
    }

    public static Token space() {
        return new Token(Space, null, ' ');
    }

    public static Token newLine() {
        return new Token(Space, null, '\n');
    }

    public static Token command(String name) {
        return new Token(Command, name, '\0');
    }

    public static Token argument(String argument) {
        return new Token(Argument, argument, '\0');
    }

    private TokenType type;

    private String s;
    private char c;

    private Token(TokenType type, String s, char c) {
        this.type = type;
        this.s = s;
        this.c = c;
    }

    public TokenType getType() {
        return type;
    }

    public String getS() {
        return s;
    }

    public char getC() {
        return c;
    }

    public String toString() {
        if (type == Space)
            if (c == '\n')
                return "Space \\n";
            else return "Space .";
        return type + (s != null ? " " + s : "") + (c != '\0' ? " " + c : "");
    }
}
