package ru.ipo.daedal.pack;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import ru.ipo.daedal.Context;
import ru.ipo.daedal.DiplomaSettings;
import ru.ipo.daedal.commands.compiler.CompilerContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PackDocument implements AutoCloseable {

    private final Document doc;
    private final PdfWriter writer;
    private final Map<String, Image> bgPath2Image = new HashMap<>();
    private final String baseFolder;

    public PackDocument(String outputFile, String baseFolder) throws FileNotFoundException, DocumentException {
        this.baseFolder = baseFolder;
        doc = new Document(
                new Rectangle(
                        //TODO implement doc size and positions
                        Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(297)
                ),
                0, 0, 0, 0
        );
        doc.open();
        writer = PdfWriter.getInstance(doc, new FileOutputStream(outputFile));
    }

    public void addPage(CompilerContext cc) throws DocumentException {
        doc.newPage();

        DiplomaSettings settings = cc.getDiplomaSettings();
        Context context = new Context(settings, null, writer, new File(baseFolder));

        String imageFileName = settings.getBg();
        Image image = null;
        if (imageFileName != null) {
            String imageLocation = baseFolder + "/" + imageFileName;

            image = bgPath2Image.computeIfAbsent(imageLocation, loc -> {
                try {
                    Image i = Image.getInstance(loc);
                    i.setAbsolutePosition(0, 0);
                    i.scaleAbsolute(Utilities.millimetersToPoints(210), Utilities.millimetersToPoints(297));
                    return i;
                } catch (Exception e) {
                    return null;
                }
            });
        }

        if (image != null)
            doc.add(image);

        cc.execInstructions(context);
    }

    @Override
    public void close() {
        doc.close();
        writer.close();
    }
}
