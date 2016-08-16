package ru.ipo.daedal.commands.compiler;

import ru.ipo.daedal.Context;
import ru.ipo.daedal.DiplomaSettings;
import ru.ipo.daedal.commands.interpreter.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 15:39.
 */
public class CompilerContext {

    private List<Instruction> instructions = new ArrayList<>();
    private DiplomaSettings diplomaSettings = new DiplomaSettings();

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    public DiplomaSettings getDiplomaSettings() {
        return diplomaSettings;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void execInstructions(Context context) {
        for (Instruction instruction : instructions)
            instruction.exec(context);
    }
}
