package ru.ipo.daedal.commands.compiler;

import ru.ipo.daedal.commands.Arguments;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:28.
 */
public interface CompilerCommand {

    int getArgumentsCount();

    void exec(CompilerContext context, Arguments args);

}
