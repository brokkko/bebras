package ru.ipo.daedal.commands.compiler;

import ru.ipo.daedal.commands.Arguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 12:23.
 */
public class CoreCommands {

    public static final CoreCommands instance = new CoreCommands();

    private Map<String, CompilerCommand> commands = new HashMap<>();

    private CoreCommands() {
        commands.put("Format", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
//                context.getDiplomaSettings().setWidth(args.getLength(0));
//                context.getDiplomaSettings().setHeight(args.getLength(1));
            }
        });

        commands.put("BG", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 0;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {

            }
        });
    }

    public Map<String, CompilerCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
}
