package ru.ipo.daedal;

import ru.ipo.daedal.commands.interpreter.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 9:51.
 */
public class ParserResult {

    private List<Instruction> commands = new ArrayList<>();

    public void add(Instruction command) {
        commands.add(command);
    }

    public List<Instruction> getCommands() {
        return commands;
    }

    public void exec(Context context) {
        for (Instruction command : commands)
            command.exec(context);
    }
}
