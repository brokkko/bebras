package ru.ipo.daedal.commands.interpreter;

import ru.ipo.daedal.Context;
import ru.ipo.daedal.commands.Arguments;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 16:36.
 */
public class WriteTextLine implements InterpreterCommand {
    @Override
    public void exec(Context context, Arguments args) {
        context.getCanvas().showTextAligned(context.getAlign(), args.get(0), context.getTextX(), context.getTextY(), 0);
    }
}
