package ru.ipo.daedal;

import ru.ipo.daedal.commands.Arguments;
import ru.ipo.daedal.commands.compiler.CompilerCommand;
import ru.ipo.daedal.commands.compiler.CompilerContext;
import ru.ipo.daedal.commands.compiler.CoreCommands;
import ru.ipo.daedal.commands.interpreter.Instruction;
import ru.ipo.daedal.commands.interpreter.WriteTextLine;

import java.io.IOException;
import java.util.Map;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:06.
 */
public class DaedalParser {

    private Map<String, CompilerCommand> commands;
    private ExpressionEvaluator evaluator;

    public DaedalParser(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
        commands = CoreCommands.instance.getCommands();
    }

    public void addCommand(String name, CompilerCommand command) {
        commands.put(name, command);
    }

    public CompilerContext parse(String code) throws IOException {
        DaedalTokenizer in = new DaedalTokenizer(code, commands, evaluator);
        CompilerContext cc = new CompilerContext();

        StringBuilder text = new StringBuilder();

        boolean inSpace = true;
        int newLinesCount = 0;

        tokensLoop:
        while (true) {
            Token token = in.next();
            switch (token.getType()) {
                case Text:
                    if (inSpace && text.length() > 0 && newLinesCount < 2)
                        text.append(' ');
                    if (newLinesCount >= 2)
                        newTextLine(text, cc);
                    inSpace = false;
                    newLinesCount = 0;

                    text.append(token.getC());
                    break;
                case Command:
                    if (newLinesCount >= 2)
                        newTextLine(text, cc);
                    inSpace = false;
                    newLinesCount = 0;

                    readCommand(in, token.getS(), cc);
                    break;
                case Space:
                    inSpace = true;
                    if (token.getC() == '\n')
                        newLinesCount++;
                    break;
                case Argument:
                    throw new DaedalParserError("Unexpected argument " + token.getS());
                case EOF:
                    newTextLine(text, cc);
                    break tokensLoop;
            }
        }

        return cc;
    }

    private void newTextLine(StringBuilder text, CompilerContext cc) {
        if (text.length() != 0)
            cc.addInstruction(new Instruction(new WriteTextLine(), new Arguments(text.toString())));
        text.setLength(0);
    }

    private void readCommand(DaedalTokenizer in, String commandName, CompilerContext cc) throws IOException {
        CompilerCommand command = commands.get(commandName);
        if (command == null)
            throw new DaedalParserError("Unknown command " + commandName);
        int argumentsCount = command.getArgumentsCount();
        String[] args = new String[argumentsCount];
        for (int i = 0; i < argumentsCount; i++) {
            Token t = in.next();
            if (t.getType() != TokenType.Argument)
                throw new DaedalParserError("Missing arguments for command " + commandName);
            args[i] = t.getS();
        }
        command.exec(cc, new Arguments(args));
    }

}
