package ru.ipo.daedal;

import ru.ipo.daedal.commands.Command;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.ipo.daedal.ParserState.*;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:06.
 */
public class DaedalParser {

    private PrependingString in;
    private ParserState state;
    private Map<String, Command> commands;
//    private ParserResult result = null;
    private StringBuilder text;
    private int newLines = 0;

    public DaedalParser(String code) {
        this.in = new PrependingString();
        this.in.prepend(code);

//        result = new ParserResult();

        state = ReadSpaces;

        initCommands();
    }

    private void initCommands() {
        commands = new HashMap<>();
//        addCommand("BG", new BgCommand());
//        addCommand("Format", new FormatCommand());
//        addCommand("VSkip", new FormatCommand());
    }

    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }

    public void parse() throws IOException {
//        if (result != null)
//            throw new DaedaelParserError("This parser has already parsed");

        while (state != Finished) {
            int out = in.read();

            if (out == -1) {
                addTextLine();
                state = Finished;
                break;
            }

            char c = (char) out;

            switch (state) {
                case ReadText:
                    state = readText(c);
                    break;
                case ReadSpaces:
                    state = readSpaces(c);
                    break;
                case EscapeRead:
                    state = escapeRead(c);
                    break;
                case ReadCommand:
                    state = readCommand(c);
                    break;
                case ReadExpression:
                    state = readExpression(c);
                    break;
                case Finished:
                    break;
            }
        }
    }

    private ParserState readText(char c) {
        if (c == '\\')
            return EscapeRead;

        if (Character.isWhitespace(c)) {
            if (c == '\n')
                newLines = 1;
            return ReadSpaces;
        }

        text.append(c);
        return ReadText;
    }

    private ParserState readSpaces(char c) {
        if (c == '\\')
            return EscapeRead;

        if (Character.isWhitespace(c)) {
            if (c == '\n')
                newLines++;
            return ReadSpaces;
        }

        if (newLines <= 1 && text.length() > 0)
            text.append(' ');
        if (newLines >= 2)
            addTextLine();

        text.append(c);
        return ReadText;
    }

    private ParserState escapeRead(char c) {
//        if (c == '\\')
        return null;
    }

    private ParserState readCommand(int out) {
        return null;
    }

    private ParserState readExpression(int out) {
        return null;
    }

    private void addTextLine() {
        if (text.length() == 0)
            return;
//        result.add(new Instruction(new TextLineCommand(), new String[]{text.toString()}));
        text.setLength(0);
    }

}
