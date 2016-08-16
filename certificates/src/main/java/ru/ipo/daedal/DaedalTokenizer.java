package ru.ipo.daedal;

import ru.ipo.daedal.commands.compiler.CompilerCommand;

import java.io.IOException;
import java.util.Map;

import static ru.ipo.daedal.Token.*;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 21:37.
 */
public class DaedalTokenizer {

    private PrependingString in;
    private Map<String, CompilerCommand> commands;
    private int argsLeft = 0;

    public DaedalTokenizer(String code, Map<String, CompilerCommand> commands, ExpressionEvaluator evaluator) throws IOException {
        this.in = expandText(code, evaluator);
        this.commands = commands;
    }

    public Token next() throws IOException {
        if (argsLeft > 0) {
            argsLeft--;
            return readArgument();
        }

        int next = in.read();

        if (next == -1)
            return eof();

        if (next == '\\') {

            if (in.peek() == -1)
                throw new DaedalParserError("Escaped end of file?");

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
                if (peek == -1 || Character.isWhitespace(peek) || peek == '/' || peek == '\\') {
                    if (peek == '/')
                        in.read();
                    break;
                }
                commandBuilder.append((char) peek);
                in.read();
            }
            if (commandBuilder.length() == 0)
                throw new DaedalParserError("Escaped nothing?");

            String command = commandBuilder.toString();
            CompilerCommand cmd = commands.get(command);
            if (cmd == null)
                throw new DaedalParserError("Unknown command \\" + command);
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

    /*private String readExpression() {
        //read until the next }
        StringBuilder exprBuilder = new StringBuilder();
        while (in.peek() != -1 && in.peek() != '}')
            exprBuilder.append((char) in.read());
        if (in.peek() == -1)
            throw new DaedalParserError("Eof reached inside expression");
        in.read(); //read }
        return exprBuilder.toString();
    }*/

    private Token readArgument() {
        //skip all spaces then
        //1) if " then read until next "
        //2) otherwise read until next space or eof

        while (in.peek() != -1 && Character.isWhitespace(in.peek()))
            in.read();

        if (in.peek() == -1)
            throw new DaedalParserError("Eof instead of argument");

        StringBuilder argBuilder = new StringBuilder();

        if (in.peek() == '"') {
            in.read();
            while (in.peek() != -1 && in.peek() != '"')
                argBuilder.append((char) in.read());
            if (in.peek() == -1)
                throw new DaedalParserError("Eof reached inside an argument");
            in.read(); //read "
            return argument(argBuilder.toString());
        } else {
            while (in.peek() != -1 && !Character.isWhitespace(in.peek()))
                argBuilder.append((char) in.read());
        }

        return argument(argBuilder.toString());
    }

    private PrependingString expandText(String text, ExpressionEvaluator evaluator) {
        PrependingString result = new PrependingString();
        result.prepend(text);

        while (true) {
            int peek = result.peek();
            if (peek == -1)
                break;
            if (peek != '{') {
                result.read();
                continue;
            }
            //now we have {, read until the next }
            result.read(); //read {

            if (result.peek() == '{') {
                result.read();
                result.prepend("", 1);
                continue;
            }

            StringBuilder expr = new StringBuilder();
            while (result.peek() != -1 && result.peek() != '}')
                expr.append((char) result.read());
            if (result.peek() == -1)
                throw new DaedalParserError("Eof reached inside an expression");
            result.read(); // read }

            String eval = evaluator.eval(expr.toString());
            result.prepend(eval, expr.length() + 2);
        }

        result.reset();
        return result;
    }
}
