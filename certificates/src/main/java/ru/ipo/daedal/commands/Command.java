package ru.ipo.daedal.commands;

import ru.ipo.daedal.Context;
import ru.ipo.daedal.DaedalParser;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:28.
 */
public interface Command {

    int getArgumentsCount();

    void exec(Context parser, Arguments args);

}
