package ru.ipo.daedal;

import com.itextpdf.text.DocumentException;
import org.junit.Test;
import ru.ipo.daedal.commands.compiler.CompilerContext;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Project: dces2
 * Created by ilya on 16.08.16, 0:05.
 */
public class TestParser {

    @Test
    public void createCertificate() throws IOException, DocumentException {
        ExpressionEvaluator evaluator = s -> "[" + s + "]";
        DaedalParser parser = new DaedalParser(evaluator);
        CompilerContext cc = parser.parse(ResourceReader.resourceToString("/ru/ipo/daedal/parse1.daedal"));
        assertEquals(cc.getDiplomaSettings().getWidth().toString(), "210.0mm");
        assertEquals(cc.getDiplomaSettings().getHeight().toString(), "99.0mm");
        assertEquals(cc.getDiplomaSettings().getBg(), "bebras-certificate-org-bg.png");

        File tempFile = File.createTempFile("doc-", ".pdf");

        try (Context context = new Context(
                cc.getDiplomaSettings(),
                new FileOutputStream(tempFile),
                new File("/mnt/ubuntu-home/ilya/programming/dces2/data/_plugins/DaedalDiploma"))
        ) {
            cc.execInstructions(context);
        }

        Desktop.getDesktop().open(tempFile);
    }

}
