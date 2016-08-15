package ru.ipo.daedal;

import ru.ipo.daedal.commands.Command;

import java.io.IOException;
import java.util.Map;

import static ru.ipo.daedal.Token.*;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 21:37.
 */
public class DaedalTokenizer {

    private PrependingString in;
    private int argsLeft = 0;
    private Map<String, Command> commands;
    private ExpressionEvaluator evaluator;

    public DaedalTokenizer(PrependingString in, Map<String, Command> commands, ExpressionEvaluator evaluator) throws IOException {
        this.in = in;
        this.commands = commands;
        this.evaluator = evaluator;
    }

    public boolean hasNext() {
        return in.peek() != -1;
    }

    public Token next() throws IOException {
        while (true) { //loop while evaluating expressions
            if (in.peek() == '{') {
                in.read();
                String expr = readExpression();
                String eval = evaluator.eval(expr);
                in.prepend(eval);
                continue;
            }

            if (argsLeft > 0) {
                argsLeft--;
                return readArgument();
            }

            int next = in.read();

            if (next == '\\') {

                if (in.peek() == -1)
                    throw new DaedaelParserError("Escaped end of file?");

                if (in.peek() == '\\') {
                    in.read();
                    return text('\\');
                }

                if (in.peek() == '{') {
                    in.read();
                    return text('{');
                }

                //otherwise reading command
                StringBuilder commandBuilder = new StringBuilder();
                while (true) {
                    int peek = in.peek();
                    if (peek == -1 || Character.isWhitespace(peek) || peek == '/' || peek ==     '\\') {
                        if (peek == '/')
                            in.read();
                        break;
                    }
                    commandBuilder.append((char) peek);
                    in.read();
                }
                if (commandBuilder.length() == 0)
                    throw new DaedaelParserError("Escaped nothing?");

                String command = commandBuilder.toString();
                Command cmd = commands.get(command);
                if (cmd == null)
                    throw new DaedaelParserError("Unknown command \\" + command);
                argsLeft = cmd.getArgumentsCount();

                return command(command);
            }

            //support \r\n as newline
            if (next == '\r') {
                if (in.peek() == -1)
                    return space();
                if (in.peek() == '\n') {
                    in.read();
                    return newLine();
                }

                return space();
            }

            if (next == '\n') {
                return newLine();
            }

            if (Character.isWhitespace(next)) {
                return space();
            }

            return text((char) next);
        }
    }

    private String readExpression() {
        //read until the next }
        StringBuilder exprBuilder = new StringBuilder();
        while (in.peek() != -1 && in.peek() != '}')
            exprBuilder.append((char) in.read());
        if (in.peek() == -1)
            throw new DaedaelParserError("Eof reached inside expression");
        in.read(); //read }
        return exprBuilder.toString();
    }

    private Token readArgument() {
        //skip all spaces then
        //1) if " then read until next "
        //2) otherwise read until next space or eof

        while (in.peek() != -1 && Character.isWhitespace(in.peek()))
            in.read();

        if (in.peek() == -1)
            throw new DaedaelParserError("Eof instead of argument");

        StringBuilder argBuilder = new StringBuilder();

        if (in.peek() == '"') {
            in.read();
            while (in.peek() != -1 && in.peek() != '"')
                argBuilder.append((char) in.read());
            if (in.peek() == -1)
                throw new DaedaelParserError("Eof reached inside argument");
            in.read(); //read "
            return argument(argBuilder.toString());
        } else {
            while (in.peek() != -1 && !Character.isWhitespace(in.peek()))
                argBuilder.append((char) in.read());
        }

        return argument(argBuilder.toString());
    }
}
