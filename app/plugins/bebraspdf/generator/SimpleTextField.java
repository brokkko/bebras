package plugins.bebraspdf.generator;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.IOException;


/**
 * Тестовое поле для ФИО, etc
 */
public class SimpleTextField implements PdfPCellEvent {

    protected String initialValue;

    protected String name;

    protected boolean visible;

    public SimpleTextField(String name, String initialValue, boolean visible) {
        this.initialValue = initialValue;
        this.name = name;
        this.visible=visible;
    }


    public void cellLayout(PdfPCell cell, Rectangle rectangle, PdfContentByte[] canvases) {
        PdfWriter writer = canvases[0].getPdfWriter();


        TextField text = new TextField(writer, rectangle, name);
        text.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
        text.setBorderColor(BaseColor.DARK_GRAY);
        text.setBorderWidth(1);
        text.setBackgroundColor(BaseColor.WHITE);
        text.setText(initialValue);
        text.setFontSize(GeneratorUtils.DEFAULT_FONT_SIZE);
        text.setAlignment(Element.ALIGN_LEFT);
        text.setOptions(TextField.REQUIRED);
        text.setFont(GeneratorUtils.getBaseFont());
        if(!visible){
            text.setVisibility(TextField.HIDDEN);
        }

        try {
            PdfFormField field = text.getTextField();
            writer.addAnnotation(field);
        } catch (IOException | DocumentException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }


}