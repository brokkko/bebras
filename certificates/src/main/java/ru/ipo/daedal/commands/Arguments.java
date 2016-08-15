package ru.ipo.daedal.commands;

import ru.ipo.daedal.DaedaelParserError;
import ru.ipo.daedal.Length;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 23:25.
 */
public class Arguments {

    private final String[] args;

    public Arguments(String... args) {
        this.args = args;
    }

    public String get(int index) {
        return args[index];
    }

    public Length getLength(int index) {
        return Length.parse(args[index]);
    }

    public int getInt(int index) {
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            throw new DaedaelParserError("Not an int: " + args[index]);
        }
    }

    public float getFloat(int index) {
        try {
            return Float.parseFloat(args[index]);
        } catch (NumberFormatException e) {
            throw new DaedaelParserError("Not a float: " + args[index]);
        }
    }

}
