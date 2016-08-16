package ru.ipo.daedal.commands.compiler;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.BaseFont;
import ru.ipo.daedal.DaedalParserError;
import ru.ipo.daedal.commands.Arguments;
import ru.ipo.daedal.commands.interpreter.Instruction;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: dces2
 * Created by ilya on 15.08.16, 12:23.
 */
public class CoreCommands {

    public static final CoreCommands instance = new CoreCommands();

    private Map<String, CompilerCommand> commands = new HashMap<>();

    private CoreCommands() {
        commands.put("format", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
                context.getDiplomaSettings().setWidth(args.getLength(0));
                context.getDiplomaSettings().setHeight(args.getLength(1));
            }
        });

        commands.put("bg", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 1;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
                context.getDiplomaSettings().setBg(args.get(0));
            }
        });

        commands.put("font", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
                context.addInstruction(new Instruction(
                        (c, a) -> {
                            try {
                                String fontFile = c.getBaseFolder().getAbsolutePath() + '/' + a.get(0);
                                BaseFont font = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                                c.getCanvas().setFontAndSize(font, args.getLength(1).getInPoints());
                            } catch (DocumentException | IOException e) {
                                throw new DaedalParserError("Error creating font " + a.get(0), e);
                            }
                        },
                        args
                ));
            }
        });

        commands.put("align", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 1;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
                context.addInstruction(new Instruction(
                        (c, a) -> {
                            switch (args.get(0)) {
                                case "left": c.setAlign(Element.ALIGN_LEFT); break;
                                case "right": c.setAlign(Element.ALIGN_RIGHT); break;
                                case "center": c.setAlign(Element.ALIGN_CENTER); break;
                                default: throw new DaedalParserError("Unknown alignment " + args.get(0));
                            }
                        },
                        args
                ));
            }
        });

        commands.put("move", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 2;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
                context.addInstruction(new Instruction(
                        (c, a) -> {
                            c.setTextX(args.getLength(0).getInPoints());
                            c.setTextY(args.getLength(1).getInPoints());
                        },
                        args
                ));
            }
        });

        commands.put("vskip", new CompilerCommand() {
            @Override
            public int getArgumentsCount() {
                return 1;
            }

            @Override
            public void exec(CompilerContext context, Arguments args) {
                context.addInstruction(new Instruction(
                        (c, a) -> c.setTextY(c.getTextY() - args.getLength(0).getInPoints()),
                        args
                ));
            }
        });
    }

    public Map<String, CompilerCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
}
