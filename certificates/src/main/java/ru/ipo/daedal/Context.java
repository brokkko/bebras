package ru.ipo.daedal;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 7:59.
 *
 * Used when diplomas are executed
 */
public class Context {

    private DiplomaSettings diplomaSettings;
    private Document document;
    private PdfWriter writer;

    private Map<String, Object> variables = new HashMap<>();
    private ExpressionEvaluator evaluator;

    private float textX = 0;
    private float textY = 0;

    public Context(DiplomaSettings diplomaSettings, OutputStream output, ExpressionEvaluator evaluator) throws DocumentException {
        this.diplomaSettings = diplomaSettings;
        this.document = diplomaSettings.createDocument();
        this.writer = PdfWriter.getInstance(document, output);
        this.evaluator = evaluator;
    }

    public Context(DiplomaSettings diplomaSettings, Document document, PdfWriter writer, ExpressionEvaluator evaluator) {
        this.diplomaSettings = diplomaSettings;
        this.document = document;
        this.writer = writer;
        this.evaluator = evaluator;
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

    public String eval(String expression) {
        return evaluator.eval(expression);
    }

    public <T> T getVar(String name) {
        //noinspection unchecked
        return (T) variables.get(name);
    }

    public <T> void setVar(String name, T value) {
        variables.put(name, value);
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
