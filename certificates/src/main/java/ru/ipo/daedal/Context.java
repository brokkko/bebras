package ru.ipo.daedal;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 7:59.
 *
 * Used when diplomas are executed
 */
public class Context implements AutoCloseable {

    private DiplomaSettings diplomaSettings;
    private Document document;
    private PdfWriter writer;
    private File baseFolder;

    private float textX = 0;
    private float textY = 0;
    private int align = Element.ALIGN_LEFT;

    private boolean isOpen = false;

    public Context(DiplomaSettings diplomaSettings, OutputStream output, File baseFolder) throws DocumentException, IOException {
        this.diplomaSettings = diplomaSettings;
        this.document = diplomaSettings.createDocument();
        this.writer = PdfWriter.getInstance(document, output);
        this.baseFolder = baseFolder;

        open();
    }

    public Context(DiplomaSettings diplomaSettings, Document document, PdfWriter writer, File baseFolder) {
        this.diplomaSettings = diplomaSettings;
        this.document = document;
        this.writer = writer;
        this.baseFolder = baseFolder;
    }

    private void open() throws IOException, DocumentException {
        document.open();
        document.newPage();

        String bgPath = diplomaSettings.getBg();
        if (bgPath != null) {
            Image bg = Image.getInstance(baseFolder.getAbsolutePath() + "/" + bgPath);
            bg.setAbsolutePosition(0, 0);
            bg.scaleAbsolute(diplomaSettings.getWidth().getInPoints(), diplomaSettings.getHeight().getInPoints());
            document.add(bg);
        }

        PdfContentByte canvas = getCanvas();
        canvas.saveState();
        canvas.beginText();

        isOpen = true;
    }

    @Override
    public void close() {
        if (!isOpen)
            return;

        PdfContentByte canvas = getCanvas();
        canvas.endText();
        canvas.restoreState();

        document.close();
    }

    public DiplomaSettings getDiplomaSettings() {
        return diplomaSettings;
    }

    public Document getDocument() {
        return document;
    }

    public PdfWriter getWriter() {
        return writer;
    }

    public PdfContentByte getCanvas() {
        return writer.getDirectContent();
    }

    public float getTextX() {
        return textX;
    }

    public float getTextY() {
        return textY;
    }

    public void setTextX(float textX) {
        this.textX = textX;
    }

    public void setTextY(float textY) {
        this.textY = textY;
    }

    public File getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    /*public void doWithWriter(Consumer<PdfWriter> action) {
        action.accept(writer);
    }

    public void doWithCanvas(Consumer<PdfContentByte> action) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();

        action.accept(canvas);

        canvas.restoreState();
    }

    public void textOnCanvas(Consumer<PdfContentByte> action) {
        doWithCanvas(canvas -> {
            canvas.beginText();
            canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_CLIP);

            action.accept(canvas);

            canvas.endText();
        });
    }*/
}
