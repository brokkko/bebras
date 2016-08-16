package ru.ipo.daedal;

import org.testng.annotations.Test;
import ru.ipo.daedal.commands.Arguments;
import ru.ipo.daedal.commands.compiler.CompilerCommand;
import ru.ipo.daedal.commands.compiler.CompilerContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;
import static ru.ipo.daedal.ResourceReader.resourceToString;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 10:57.
 */
public class TestTokenizer {

    @Test
    public void test1() throws IOException {
        String _1 = resourceToString("/ru/ipo/daedal/1.daedal");

        DaedalTokenizer tokenizer = new DaedalTokenizer(_1, createTestCommands(), (s) -> "");

        assertEquals(getAllTokens(tokenizer), resourceToString("/ru/ipo/daedal/1.tokens"));
    }

    @Test
    public void test2() throws IOException {
        String _2 = resourceToString("/ru/ipo/daedal/2.daedal");

        ExpressionEvaluator expressionEvaluator = (s) -> {
            if (s.equals("make empty"))
                return "";
            else
                return "" + s.length();
        };
        DaedalTokenizer tokenizer = new DaedalTokenizer(_2, createTestCommands(), expressionEvaluator);

        assertEquals(getAllTokens(tokenizer), resourceToString("/ru/ipo/daedal/2.tokens"));
    }

    @Test
    public void test3() throws IOException {
        String _3 = resourceToString("/ru/ipo/daedal/3.daedal");

        ExpressionEvaluator expressionEvaluator = (s) -> {
            if (s.equals("make empty"))
                return "";
            else
                return "" + s.length();
        };
        DaedalTokenizer tokenizer = new DaedalTokenizer(_3, createTestCommands(), expressionEvaluator);

        assertEquals(getAllTokens(tokenizer), resourceToString("/ru/ipo/daedal/3.tokens"));
    }

    private String getAllTokens(DaedalTokenizer tokenizer) throws IOException {
        StringBuilder result = new StringBuilder();
        while (true) {
            Token next = tokenizer.next();
            if (next.getType() == TokenType.EOF)
                break;
            result.append(next.toString()).append('\n');
        }
        return result.toString();
    }

    private Map<String, CompilerCommand> createTestCommands() {
        Map<String, CompilerCommand> commands = new HashMap<>();
        commands.put("a", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 0;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {

            }
        });
        commands.put("bb", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 1;
            }

            @Override
            public void exec(CompilerContext p, Arguments args) {

            }
        });
        commands.put("ccc", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(CompilerContext p, Arguments args) {

            }
        });
        return commands;
    }

}
