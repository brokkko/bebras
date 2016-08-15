package ru.ipo.daedal.commands.compiler;

import ru.ipo.daedal.commands.interpreter.Instruction;

import java.util.List;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 15:39.
 */
public class CompilerContext {

    private List<Instruction> instructions;

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }
}
