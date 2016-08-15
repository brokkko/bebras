package ru.ipo.daedal.commands;

import ru.ipo.daedal.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 12:23.
 */
public class CoreCommands {

    public static final CoreCommands instance = new CoreCommands();

    private Map<String, Command> commands = new HashMap<>();

    private CoreCommands() {
        commands.put("Format", new Command() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(Context context, Arguments args) {
                context.getDiplomaSettings().setWidth(args.getLength(0));
                context.getDiplomaSettings().setHeight(args.getLength(1));
            }
        });

        commands.put("BG", new Command() {
            @Override
            public int getArgumentsCount() {
                return 0;
            }

            @Override
            public void exec(Context parser, Arguments args) {

            }
        });
    }

    public Map<String, Command> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
}
