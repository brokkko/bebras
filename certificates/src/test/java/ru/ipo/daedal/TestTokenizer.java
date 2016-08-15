package ru.ipo.daedal;

import org.testng.annotations.Test;
import ru.ipo.daedal.commands.Arguments;
import ru.ipo.daedal.commands.Command;

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

        PrependingString ps = new PrependingString();
        ps.prepend(_1);

        DaedalTokenizer tokenizer = new DaedalTokenizer(ps, createTestCommands(), (s) -> "");

        StringBuilder result = new StringBuilder();
        while (tokenizer.hasNext())
            result.append(tokenizer.next().toString()).append('\n');

        assertEquals(result.toString(), resourceToString("/ru/ipo/daedal/1.tokens"));
    }

    @Test
    public void test2() throws IOException {
        String _2 = resourceToString("/ru/ipo/daedal/2.daedal");

        PrependingString ps = new PrependingString();
        ps.prepend(_2);

        DaedalTokenizer tokenizer = new DaedalTokenizer(ps, createTestCommands(), (s) -> "" + s.length());

        StringBuilder result = new StringBuilder();
        while (tokenizer.hasNext())
            result.append(tokenizer.next().toString()).append('\n');

        assertEquals(result.toString(), resourceToString("/ru/ipo/daedal/2.tokens"));
    }

    private Map<String, Command> createTestCommands() {
        Map<String, Command> commands = new HashMap<>();
        commands.put("a", new Command() {
            @Override
            public int getArgumentsCount() {
                return 0;
            }

            @Override
            public void exec(Context p, Arguments args) {

            }
        });
        commands.put("bb", new Command() {
            @Override
            public int getArgumentsCount() {
                return 1;
            }

            @Override
            public void exec(Context p, Arguments args) {

            }
        });
        commands.put("ccc", new Command() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(Context p, Arguments args) {

            }
        });
        return commands;
    }

}
