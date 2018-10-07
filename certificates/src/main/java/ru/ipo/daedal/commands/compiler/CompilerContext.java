package ru.ipo.daedal.commands.compiler;

import ru.ipo.daedal.Context;
import ru.ipo.daedal.DiplomaSettings;
import ru.ipo.daedal.Token;
import ru.ipo.daedal.commands.interpreter.Instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 15:39.
 */
public class CompilerContext {

    private List<Instruction> instructions = new ArrayList<>();
    private DiplomaSettings diplomaSettings = new DiplomaSettings();
    private List<Token> extraTokens = new ArrayList<>();

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

    public boolean hasExtraToken() {
        return !extraTokens.isEmpty();
    }

    public Token popExtraToken() {
        return extraTokens.remove(0);
    }

    public void prependExtraTokens(List<Token> tokens) {
        extraTokens.addAll(0, tokens);
    }

    public void prependExtraTokens(Token... tokens) {
        prependExtraTokens(Arrays.asList(tokens));
    }

    public void prependTextAsTokens(String text) {
        List<Token> tokens = new ArrayList<>();
        for (char c : text.toCharArray())
            tokens.add(Token.text(c));
        prependExtraTokens(tokens);
    }
}
