package ru.ipo.daedal.commands.interpreter;

import ru.ipo.daedal.Context;
import ru.ipo.daedal.commands.Arguments;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 15:18.
 */
@FunctionalInterface
public interface InterpreterCommand {
    void exec(Context context, Arguments args);
}
