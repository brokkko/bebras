package ru.ipo.daedal.commands.interpreter;

import ru.ipo.daedal.Context;
import ru.ipo.daedal.commands.Arguments;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 15:19.
 */
public class Instruction {
    private InterpreterCommand command;
    private Arguments arguments;

    public Instruction(InterpreterCommand command, Arguments arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public void exec(Context context) {
        command.exec(context, arguments);
    }
}
